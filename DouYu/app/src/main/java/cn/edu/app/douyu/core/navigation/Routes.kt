package cn.edu.app.douyu.core.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class BottomTab(
    val route: String,
    val label: String,
    val iconSemantic: String
) {
    COMMUNITY("community", "社区", "community"),
    COMMERCE("commerce", "商城", "shop"),
    AI("ai", "AI", "ai"),
    MESSAGE(
        "message",
        "消息",
        "message"
    ),
    PROFILE("profile", "我的", "profile")
}

object AppRoute {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val LOGIN_ROUTE = "login_return?returnTo={returnTo}"
    const val POST_DETAIL = "post/{postId}"
    const val POST_CREATE = "post_create"
    const val IMAGE_SELECT = "image_select"
    const val IMAGE_SELECT_ROUTE = "image_select?openCamera={openCamera}"
    const val CAMERA_CAPTURE = "camera_capture"
    const val AI_PARAMS = "ai_params/{uploadedFileId}"
    const val AI_PROGRESS = "ai_progress/{jobId}"
    const val PATTERN_RESULT = "pattern/{patternId}"
    const val PATTERN_HISTORY = "pattern_history"
    const val PRODUCT_LIST = "product_list"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val CART = "cart"
    const val ORDER_CONFIRM = "order_confirm"
    const val PAYMENT_RESULT = "payment_result/{orderId}"
    const val CONVERSATION = "conversation/{conversationId}"
    const val MY_PATTERNS = "my_patterns"
    const val FAVORITES = "favorites"
    const val LIKED_POSTS = "liked_posts"
    const val COMMENTED_POSTS = "commented_posts"
    const val FAVORITE_POSTS = "favorite_posts"
    const val FOLLOWED_POSTS = "followed_posts"
    const val MY_ORDERS = "my_orders"
    const val SETTINGS = "settings"
    const val SETTINGS_SECTION = "settings_section/{section}"
    const val SEARCH = "search"
    const val PROFILE_EDIT = "profile_edit"
    const val NOTIFICATION_DETAIL = "notification_detail/{notificationId}?title={title}&content={content}&type={type}&createdAt={createdAt}"

    val profileInteractionPostAssetRoutes = listOf(
        LIKED_POSTS,
        COMMENTED_POSTS,
        FAVORITE_POSTS,
        FOLLOWED_POSTS
    )

    val allRoutes = listOf(
        SPLASH,
        LOGIN,
        LOGIN_ROUTE,
        POST_DETAIL,
        POST_CREATE,
        IMAGE_SELECT,
        IMAGE_SELECT_ROUTE,
        CAMERA_CAPTURE,
        AI_PARAMS,
        AI_PROGRESS,
        PATTERN_RESULT,
        PATTERN_HISTORY,
        PRODUCT_LIST,
        PRODUCT_DETAIL,
        CART,
        ORDER_CONFIRM,
        PAYMENT_RESULT,
        CONVERSATION,
        MY_PATTERNS,
        FAVORITES,
        LIKED_POSTS,
        COMMENTED_POSTS,
        FAVORITE_POSTS,
        FOLLOWED_POSTS,
        MY_ORDERS,
        SETTINGS,
        SETTINGS_SECTION,
        SEARCH,
        PROFILE_EDIT,
        NOTIFICATION_DETAIL
    ) + BottomTab.entries.map { it.route }

    private fun encode(value: String): String = URLEncoder
        .encode(value, StandardCharsets.UTF_8.name())
        .replace("+", "%20")

    fun login(returnTo: String): String {
        val encodedReturnTo = encode(returnTo)
        return "login_return?returnTo=$encodedReturnTo"
    }

    fun postDetail(postId: String) = "post/$postId"
    fun imageSelect(openCamera: Boolean = false) = if (openCamera) "image_select?openCamera=true" else IMAGE_SELECT
    fun aiParams(uploadedFileId: String) = "ai_params/$uploadedFileId"
    fun aiProgress(jobId: String) = "ai_progress/$jobId"
    fun patternResult(patternId: String) = "pattern/$patternId"
    fun productDetail(productId: String) = "product/$productId"
    fun paymentResult(orderId: String) = "payment_result/$orderId"
    fun conversation(conversationId: String) = "conversation/$conversationId"
    fun settingsSection(section: String) = "settings_section/${encode(section)}"
    fun notificationDetail(
        notificationId: String,
        title: String,
        content: String,
        type: String,
        createdAt: String
    ) = "notification_detail/${encode(notificationId)}?title=${encode(title)}&content=${encode(content)}&type=${encode(type)}&createdAt=${encode(createdAt)}"
}
