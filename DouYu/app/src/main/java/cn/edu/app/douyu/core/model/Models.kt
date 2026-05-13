package cn.edu.app.douyu.core.model

import cn.edu.app.douyu.core.network.PageResponse

enum class ContentStatus { REVIEWING, VISIBLE, SELF_VISIBLE, REJECTED, DELETED }
enum class PatternJobStatus { PENDING, PROCESSING, SUCCEEDED, FAILED, REJECTED, CANCELED }
enum class BeadSize { MM_2_6, MM_5 }
enum class PatternDifficulty { BEGINNER, NORMAL, ADVANCED }
enum class PatternStyle { RESTORE, CUTE, ANIME, LOW_COLOR, ICON }
enum class ProductType { SELF_OPERATED, PLAYER_SECOND_HAND, PLAYER_CUSTOM_SERVICE }
enum class ProductStatus { DRAFT, ON_SALE, OFF_SALE, SOLD_OUT, DELETED }
enum class OrderStatus { CREATED, WAITING_PAYMENT, PAID, FULFILLING, SHIPPED, COMPLETED, CANCELED, REFUNDING, REFUNDED }
enum class PaymentChannel { WECHAT_APP, ALIPAY_APP }
enum class PaymentStatus { CREATED, PROCESSING, SUCCEEDED, FAILED, CLOSED }

data class UserProfile(
    val id: String,
    val nickname: String,
    val avatarUrl: String?,
    val bio: String,
    val level: Int,
    val isMinor: Boolean,
    val followingCount: Int,
    val followerCount: Int
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserProfile
)

data class SmsCodeRequest(val phone: String)
data class SmsLoginRequest(val phone: String, val code: String)
data class UploadPresignRequest(val usage: String, val fileName: String, val mimeType: String)
data class UploadPresign(val uploadUrl: String, val fileKey: String, val headers: Map<String, String>, val expiresIn: Long)

data class Post(
    val id: String,
    val author: UserProfile,
    val title: String,
    val content: String,
    val mediaColors: List<Long>,
    val topic: String,
    val linkedPatternId: String?,
    val status: ContentStatus,
    val likeCount: Int,
    val favoriteCount: Int,
    val commentCount: Int
)

data class Comment(
    val id: String,
    val author: UserProfile,
    val content: String,
    val status: ContentStatus
)

data class PatternJob(
    val id: String,
    val inputName: String,
    val beadSize: BeadSize,
    val difficulty: PatternDifficulty,
    val style: PatternStyle,
    val status: PatternJobStatus,
    val progress: Int,
    val failureReason: String?,
    val patternId: String?
)

data class PatternAsset(
    val id: String,
    val title: String,
    val beadSize: BeadSize,
    val widthCells: Int,
    val heightCells: Int,
    val totalBeads: Int,
    val paletteName: String,
    val colorStats: List<PaletteColorCount>,
    val materials: List<MaterialSuggestion>
)

data class PaletteColorCount(
    val colorCode: String,
    val displayName: String,
    val hex: Long,
    val beadCount: Int
)

data class MaterialSuggestion(
    val name: String,
    val quantity: String,
    val priceCent: Int,
    val inStock: Boolean
)

data class Product(
    val id: String,
    val type: ProductType,
    val title: String,
    val description: String,
    val category: String,
    val status: ProductStatus,
    val priceCent: Int,
    val stock: Int,
    val swatchColor: Long
)

data class CartItem(
    val id: String,
    val product: Product,
    val quantity: Int
)

data class Cart(
    val items: List<CartItem>
) {
    val payableAmountCent: Int
        get() = items.sumOf { it.product.priceCent * it.quantity }
}

data class Order(
    val id: String,
    val status: OrderStatus,
    val items: List<CartItem>,
    val payableAmountCent: Int,
    val addressSnapshot: String
)

data class Payment(
    val id: String,
    val orderId: String,
    val channel: PaymentChannel,
    val status: PaymentStatus,
    val amountCent: Int
)

data class CreatePaymentRequest(val orderId: String, val channel: PaymentChannel)

data class NotificationMessage(
    val id: String,
    val title: String,
    val content: String,
    val unread: Boolean
)

data class Conversation(
    val id: String,
    val peerName: String,
    val lastMessage: String,
    val unreadCount: Int,
    val riskHint: String?
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val content: String,
    val mine: Boolean
)

data class RewardSummary(
    val level: Int,
    val exp: Int,
    val nextLevelExp: Int,
    val checkinToday: Boolean
)

data class DashboardData(
    val user: UserProfile,
    val reward: RewardSummary,
    val patternCount: Int,
    val orderCount: Int
)

data class HomeFeed(
    val items: List<Post>,
    val page: Int = 1,
    val size: Int = 20,
    val total: Int = items.size,
    val hasMore: Boolean = false
) {
    fun asPageResponse(): PageResponse<Post> = PageResponse(items, page, size, total, hasMore)
}
