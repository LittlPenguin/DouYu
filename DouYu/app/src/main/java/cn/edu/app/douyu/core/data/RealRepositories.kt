package cn.edu.app.douyu.core.data

import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.network.*
import kotlinx.coroutines.runBlocking

private fun <T> apiCall(block: suspend () -> ApiResponse<T>): T {
    val response = runBlocking { block() }
    if (!response.isOk) {
        throw IllegalStateException("API error ${response.code}: ${response.message}")
    }
    return response.data ?: throw IllegalStateException("API returned null data")
}

class RealCommunityRepository(private val api: CommunityApi) : CommunityRepository {
    override fun feed(): PageResponse<Post> = apiCall { api.feed() }
    override fun post(postId: String): Post = apiCall { api.post(postId) }
    override fun comments(postId: String): PageResponse<Comment> = apiCall { api.comments(postId) }
}

class RealPatternRepository(private val api: PatternApi) : PatternRepository {
    override fun featuredJob(): PatternJob {
        val page = apiCall { api.jobs(page = 1, size = 1) }
        return page.items.firstOrNull()
            ?: throw IllegalStateException("No pattern jobs found")
    }

    override fun history(): PageResponse<PatternJob> = apiCall { api.jobs() }
    override fun job(jobId: String): PatternJob = apiCall { api.job(jobId) }
    override fun pattern(patternId: String): PatternAsset = apiCall { api.pattern(patternId) }
}

class RealCommerceRepository(
    private val productApi: ProductApi,
    private val cartApi: CartApi,
    private val orderApi: OrderApi,
    private val paymentApi: PaymentApi
) : CommerceRepository {
    override fun products(): PageResponse<Product> = apiCall { productApi.products() }
    override fun product(productId: String): Product = apiCall { productApi.product(productId) }
    override fun cart(): Cart = apiCall { cartApi.cart() }

    override fun order(): Order {
        val page = apiCall { orderApi.orders(page = 1, size = 1) }
        return page.items.firstOrNull()
            ?: throw IllegalStateException("No orders found")
    }

    override fun payment(orderId: String): Payment {
        val order = apiCall { orderApi.order(orderId) }
        return Payment(
            paymentId = "pending_$orderId",
            orderId = orderId,
            channel = PaymentChannel.WECHAT_APP,
            status = when (order.status) {
                OrderStatus.PAID -> PaymentStatus.SUCCEEDED
                OrderStatus.CANCELED -> PaymentStatus.CLOSED
                else -> PaymentStatus.PROCESSING
            },
            amountCent = order.payableAmountCent,
            payParams = emptyMap()
        )
    }
}

class RealMessageRepository(private val api: MessageApi) : MessageRepository {
    override fun notifications(): PageResponse<NotificationMessage> = apiCall { api.notifications() }
    override fun conversations(): PageResponse<Conversation> = apiCall { api.conversations() }
    override fun chat(conversationId: String): PageResponse<ChatMessage> {
        val detail = apiCall { api.conversation(conversationId) }
        return PageResponse(detail.messages, 1, detail.messages.size, detail.messages.size, false)
    }
}

class RealProfileRepository(
    private val userApi: UserApi,
    private val rewardApi: RewardApi,
    private val patternApi: PatternApi
) : ProfileRepository {
    override fun dashboard(): DashboardData {
        val user = apiCall { userApi.me() }
        val reward = try { apiCall { rewardApi.me() } } catch (_: Exception) {
            RewardSummary()
        }
        val jobsPage = try { apiCall { patternApi.jobs(page = 1, size = 100) } } catch (_: Exception) {
            PageResponse(emptyList(), 1, 100, 0, false)
        }
        val badges = try { apiCall { rewardApi.badges() }.items } catch (_: Exception) { emptyList() }
        return DashboardData(user, reward, jobsPage.items.size, 0, badges)
    }

    override fun patterns(): List<PatternAsset> {
        val jobs = apiCall { patternApi.jobs(page = 1, size = 100) }
        return jobs.items
            .filter { it.patternId != null }
            .mapNotNull { job ->
                try { apiCall { patternApi.pattern(job.patternId!!) } } catch (_: Exception) { null }
            }
    }

    override fun checkinStatus(): CheckinStatus = apiCall { rewardApi.checkinStatus() }
    override fun badges(): List<Badge> = apiCall { rewardApi.badges() }.items
}
