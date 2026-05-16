package cn.edu.app.douyu.core.data

import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import retrofit2.HttpException

private val errorJson = Json { ignoreUnknownKeys = true; explicitNulls = false }

private fun <T> apiCall(block: suspend () -> ApiResponse<T>): T {
    val response = try {
        runBlocking(Dispatchers.IO) { block() }
    } catch (e: HttpException) {
        val parsed = runCatching {
            val body = e.response()?.errorBody()?.string().orEmpty()
            if (body.isBlank()) null
            else errorJson.decodeFromString<ApiResponse<kotlinx.serialization.json.JsonElement>>(body)
        }.getOrNull()
        throw ApiException(
            parsed?.code ?: "HTTP_${e.code()}",
            parsed?.message ?: "请求失败 (${e.code()})",
            parsed?.traceId
        )
    }
    if (!response.isOk) {
        throw ApiException(response.code, response.message, response.traceId)
    }
    @Suppress("UNCHECKED_CAST")
    return response.data ?: (Unit as T)
}

class ApiException(val code: String, override val message: String, val traceId: String?) : RuntimeException("$code: $message")

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

    override fun productsByCategory(categoryId: String): PageResponse<Product> =
        apiCall { productApi.products(categoryId = categoryId) }

    override fun product(productId: String): Product = apiCall { productApi.product(productId) }
    override fun cart(): Cart = apiCall { cartApi.cart() }

    override fun addItemToCart(productId: String, skuId: String, quantity: Int): Cart =
        apiCall { cartApi.addItem(AddCartItemRequest(productId, skuId, quantity)) }

    override fun updateCartItem(itemId: String, quantity: Int): Cart =
        apiCall { cartApi.updateItem(itemId, UpdateCartItemRequest(quantity)) }

    override fun removeCartItem(itemId: String): Cart =
        apiCall { cartApi.removeItem(itemId) }

    override fun order(): Order {
        val page = apiCall { orderApi.orders(page = 1, size = 1) }
        return page.items.firstOrNull()
            ?: throw IllegalStateException("No orders found")
    }

    override fun order(orderId: String): Order = apiCall { orderApi.order(orderId) }

    override fun createOrder(itemIds: List<String>, addressId: String): Order =
        apiCall { orderApi.createOrder(CreateOrderRequest(itemIds, addressId)) }

    override fun createPayment(orderId: String, channel: PaymentChannel): Payment =
        apiCall { paymentApi.createPayment(CreatePaymentRequest(orderId, channel)) }

    override fun paymentStatus(paymentId: String): Payment =
        apiCall { paymentApi.payment(paymentId) }
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
