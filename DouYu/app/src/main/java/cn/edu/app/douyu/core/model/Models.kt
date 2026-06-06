package cn.edu.app.douyu.core.model

import cn.edu.app.douyu.core.network.PageResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Deserializes backend Double (0.0–1.0) to frontend Int (0–100). */
object ProgressSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Progress", PrimitiveKind.DOUBLE)
    override fun deserialize(decoder: Decoder): Int {
        val v = decoder.decodeDouble()
        return (v * 100).toInt().coerceIn(0, 100)
    }
    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeDouble(value / 100.0)
}

@Serializable
enum class ContentStatus { REVIEWING, VISIBLE, SELF_VISIBLE, PRIVATE, REJECTED, DELETED }

@Serializable
enum class AuditStatus { NEED_MANUAL_REVIEW, PASS, REJECTED }

@Serializable
enum class UploadUsage { AVATAR, POST_IMAGE, POST_VIDEO, AI_INPUT, PATTERN_OUTPUT, PRODUCT_IMAGE, TRADE_IMAGE }

@Serializable
enum class PatternJobStatus { PENDING, PROCESSING, SUCCEEDED, FAILED, REJECTED, CANCELED }

@Serializable
enum class BeadSize { MM_2_6, MM_5 }

@Serializable
enum class PatternDifficulty { BEGINNER, NORMAL, ADVANCED }

@Serializable
enum class PatternStyle { RESTORE, CUTE, ANIME, LOW_COLOR, ICON }

@Serializable
enum class ProductType { SELF_OPERATED, PLAYER_SECOND_HAND, PLAYER_CUSTOM_SERVICE }

@Serializable
enum class ProductStatus { DRAFT, ON_SALE, OFF_SALE, SOLD_OUT, DELETED }

@Serializable
enum class SkuStatus { ON_SALE, OFF_SALE, SOLD_OUT, DELETED }

@Serializable
enum class SellerType { SELF_OPERATED, PLATFORM, PLAYER }

@Serializable
enum class OrderType { SELF_OPERATED, PLAYER_TRADE, CUSTOM_SERVICE }

@Serializable
enum class OrderStatus { CREATED, WAITING_PAYMENT, PAID, FULFILLING, SHIPPED, COMPLETED, CANCELED, REFUNDING, REFUNDED }

@Serializable
enum class PaymentChannel { WECHAT_APP, ALIPAY_APP }

@Serializable
enum class PaymentStatus { CREATED, PROCESSING, SUCCEEDED, FAILED, CLOSED }

@Serializable
enum class NotificationType { SYSTEM, COMMENT, LIKE, FAVORITE, FOLLOW, MENTION, ORDER, AI_TASK, REPORT }

@Serializable
data class UserProfile(
    val userId: String,
    val nickname: String,
    val avatarUrl: String? = null,
    val bio: String = "",
    val level: Int = 0,
    val isMinor: Boolean = false,
    val followingCount: Int = 0,
    val followerCount: Int = 0
)

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserProfile
)

@Serializable
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

@Serializable
data class SmsCodeRequest(val phone: String)

@Serializable
data class SmsLoginRequest(
    val phone: String,
    val code: String,
    val ageGroup: String = "AGE_18_PLUS",
    val nickname: String? = null
)

@Serializable
data class UpdateProfileRequest(
    val nickname: String,
    val avatarFileId: String? = null,
    val bio: String? = null
)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class UploadPresignRequest(
    val usage: UploadUsage,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long? = null
)

@Serializable
data class UploadPresign(
    val uploadUrl: String,
    val fileKey: String,
    val headers: Map<String, String>,
    val expiresIn: Long
)

@Serializable
data class UploadConfirmRequest(
    val fileKey: String,
    val usage: UploadUsage,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class FileAsset(
    val fileId: String,
    val ownerId: String,
    val usage: UploadUsage,
    val storageKey: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int? = null,
    val height: Int? = null,
    val auditStatus: AuditStatus,
    val publicUrl: String? = null
)

@Serializable
data class Post(
    val postId: String,
    val authorId: String,
    val author: UserProfile,
    val title: String,
    val content: String,
    val mediaFileIds: List<String>,
    val mediaColors: List<Long>,
    val topicIds: List<String>,
    val topicNames: List<String>,
    val linkedPatternId: String?,
    val status: ContentStatus,
    val likeCount: Int,
    val favoriteCount: Int,
    val commentCount: Int,
    val coverImageUrl: String? = null,
    val likedByMe: Boolean = false,
    val favoritedByMe: Boolean = false,
    val followedAuthorByMe: Boolean = false
)

@Serializable
data class CreatePostRequest(
    val title: String,
    val content: String,
    val mediaFileIds: List<String>,
    val topicIds: List<String> = emptyList(),
    val linkedPatternId: String? = null
)

@Serializable
data class Comment(
    val commentId: String,
    val postId: String,
    val authorId: String,
    val author: UserProfile,
    val parentId: String? = null,
    val content: String,
    val status: ContentStatus,
    val mediaFileIds: List<String> = emptyList(),
    val mediaAssets: List<CommentMediaAsset> = emptyList(),
    val mentions: List<CommentMention> = emptyList(),
    val topics: List<CommentTopic> = emptyList(),
    val stickers: List<Sticker> = emptyList()
)

@Serializable
data class Topic(
    val topicId: String,
    val name: String,
    val description: String = "",
    val postCount: Int = 0
)

@Serializable
data class CommentMention(
    val userId: String,
    val nickname: String,
    val avatarUrl: String? = null
)

@Serializable
data class CommentTopic(
    val topicId: String,
    val name: String
)

@Serializable
data class StickerPack(
    val packId: String,
    val name: String,
    val stickers: List<Sticker> = emptyList()
)

@Serializable
data class Sticker(
    val stickerId: String,
    val packId: String = "",
    val name: String,
    val imageUrl: String? = null,
    val emojiText: String = ""
)

@Serializable
data class CommentMediaAsset(
    val fileId: String,
    val publicUrl: String? = null,
    val mimeType: String = "",
    val width: Int? = null,
    val height: Int? = null,
    val auditStatus: AuditStatus = AuditStatus.NEED_MANUAL_REVIEW
)

@Serializable
data class CreateCommentRequest(
    val content: String,
    val parentId: String? = null,
    val mediaFileIds: List<String> = emptyList(),
    val mentionUserIds: List<String> = emptyList(),
    val topicIds: List<String> = emptyList(),
    val stickerIds: List<String> = emptyList()
)

@Serializable
data class PostInteractionResult(
    val liked: Boolean? = null,
    val favorited: Boolean? = null,
    val likeCount: Int? = null,
    val favoriteCount: Int? = null,
    val likedByMe: Boolean? = null,
    val favoritedByMe: Boolean? = null
)

@Serializable
data class CreatePatternJobRequest(
    val inputFileId: String,
    val beadSize: BeadSize,
    val targetSize: String,
    val difficulty: PatternDifficulty,
    val paletteId: String,
    val style: PatternStyle
)

@Serializable
data class PatternJob(
    val jobId: String,
    val inputFileId: String,
    val beadSize: BeadSize,
    val targetSize: String,
    val difficulty: PatternDifficulty,
    val paletteId: String,
    val style: PatternStyle,
    val status: PatternJobStatus,
    val failureReason: String? = null,
    val patternId: String? = null,
    val userId: String? = null,
    val inputName: String? = null,
    val paletteName: String? = null,
    @Serializable(with = ProgressSerializer::class) val progress: Int = 0
)

@Serializable
data class PatternAsset(
    val patternId: String,
    val jobId: String,
    val ownerId: String = "",
    val title: String = "",
    val previewFileId: String? = null,
    val gridFileId: String? = null,
    val colorMapFileId: String? = null,
    val pdfFileId: String? = null,
    val beadSize: BeadSize,
    val widthCells: Int = 0,
    val heightCells: Int = 0,
    val totalBeads: Int = 0,
    val paletteName: String = "",
    val status: ContentStatus = ContentStatus.VISIBLE,
    val colorStats: List<PaletteColorCount> = emptyList(),
    val materials: PatternMaterials? = null
)

@Serializable
data class PaletteColorCount(
    val colorCode: String,
    val displayName: String,
    val hex: Long,
    val beadCount: Int,
    val productSkuId: String? = null
)

@Serializable
data class MaterialSuggestion(
    val productId: String,
    val skuId: String,
    val name: String,
    val quantity: String,
    val priceCent: Int,
    val availableStock: Int
) {
    val inStock: Boolean
        get() = availableStock > 0
}

/** Backend materials structure: { totalBeads, colors: [{ colorCode, displayName, beadCount, skuId }] } */
@Serializable
data class PatternMaterials(
    val totalBeads: Int = 0,
    val colors: List<PatternColorEntry> = emptyList()
)

@Serializable
data class PatternColorEntry(
    val colorCode: String = "",
    val displayName: String = "",
    val beadCount: Int = 0,
    val skuId: String? = null
)

@Serializable
data class ProductSku(
    val skuId: String,
    val productId: String,
    val specName: String,
    val priceCent: Int,
    @kotlinx.serialization.SerialName("stock") val availableStock: Int,
    val status: SkuStatus
)

@Serializable
data class Product(
    val productId: String,
    val type: ProductType,
    val sellerId: String? = null,
    val title: String,
    val description: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val status: ProductStatus,
    val auditStatus: AuditStatus,
    val skus: List<ProductSku>,
    val swatchColor: Long = 0,
    val imageUrl: String? = null
) {
    val priceCent: Int
        get() = skus.minOfOrNull { it.priceCent } ?: 0

    val availableStock: Int
        get() = skus.sumOf { it.availableStock }
}

@Serializable
data class CartProductSummary(
    val title: String = "",
    val imageUrl: String? = null,
    val swatchColor: Long = 0
)

@Serializable
data class CartItem(
    val itemId: String,
    val sku: ProductSku? = null,
    val quantity: Int,
    val productId: String? = null,
    val product: CartProductSummary? = null
) {
    val lineAmountCent: Int
        get() = (sku?.priceCent ?: 0) * quantity
}

@Serializable
data class Cart(
    val items: List<CartItem>
) {
    val payableAmountCent: Int
        get() = items.sumOf { it.lineAmountCent }
}

@Serializable
data class CartMutationResult(
    val itemId: String? = null,
    val quantity: Int? = null,
    val deleted: Boolean? = null
)

@Serializable
data class AddCartItemRequest(
    val productId: String,
    val skuId: String,
    val quantity: Int
)

@Serializable
data class UpdateCartItemRequest(val quantity: Int)

@Serializable
data class CreateOrderRequest(
    val itemIds: List<String>,
    val addressId: String,
    val remark: String? = null
)

@Serializable
data class OrderItem(
    val orderItemId: String,
    val productId: String,
    val skuId: String,
    val title: String,
    val specName: String,
    val priceCent: Int,
    val quantity: Int
) {
    val lineAmountCent: Int
        get() = priceCent * quantity
}

@Serializable
data class AddressSnapshot(
    val addressId: String? = null,
    val raw: String? = null
)

@Serializable
data class Order(
    val orderId: String,
    val buyerId: String,
    val sellerType: SellerType,
    val sellerId: String? = null,
    val orderType: OrderType,
    val status: OrderStatus,
    val totalAmountCent: Int,
    val payableAmountCent: Int,
    val items: List<OrderItem>,
    val addressSnapshot: AddressSnapshot? = null
)

@Serializable
data class Payment(
    val paymentId: String,
    val orderId: String,
    val channel: PaymentChannel,
    val status: PaymentStatus,
    val amountCent: Int,
    val payParams: Map<String, String>,
    val channelTradeNo: String? = null,
    val paidAt: String? = null
)

@Serializable
data class CreatePaymentRequest(val orderId: String, val channel: PaymentChannel)

@Serializable
data class NotificationMessage(
    val notificationId: String,
    val type: NotificationType,
    val title: String,
    val content: String,
    val unread: Boolean = false,
    val createdAt: String? = null
)

@Serializable
data class MarkNotificationsReadRequest(val notificationIds: List<String>)

@Serializable
data class Conversation(
    val conversationId: String,
    val userAId: String = "",
    val userBId: String = "",
    val peerUserId: String = "",
    val peerName: String = "",
    val peerAvatarUrl: String? = null,
    val lastMessage: String = "",
    val unreadCount: Int = 0,
    val mutualFollow: Boolean = false,
    val remainingNonMutualMessages: Int = 0,
    val canSend: Boolean = false,
    val riskHint: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ConversationDetail(
    val conversation: Conversation? = null,
    val messages: List<ChatMessage> = emptyList()
)

@Serializable
data class ChatMessage(
    val messageId: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val mine: Boolean,
    val createdAt: String? = null
)

@Serializable
data class SendMessageRequest(val content: String)

@Serializable
data class FollowResult(
    val followed: Boolean = false,
    val followedByMe: Boolean = false,
    val followsMe: Boolean = false,
    val mutualFollow: Boolean = false
)

@Serializable
data class CheckinStatus(
    val checkedToday: Boolean = false,
    val alreadyChecked: Boolean = false,
    val points: Int = 0,
    val experience: Int = 0
)

@Serializable
data class Badge(
    val badgeId: String,
    val name: String,
    val description: String = "",
    val achieved: Boolean = false
)

@Serializable
data class BadgeListWrapper(
    val items: List<Badge> = emptyList()
)

@Serializable
data class RewardSummary(
    val points: Int = 0,
    val experience: Int = 0,
    val levelCode: String = ""
)

@Serializable
data class DashboardData(
    val user: UserProfile,
    val reward: RewardSummary,
    val patternCount: Int,
    val orderCount: Int,
    val badges: List<Badge>
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
