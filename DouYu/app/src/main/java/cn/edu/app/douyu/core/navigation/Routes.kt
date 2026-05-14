package cn.edu.app.douyu.core.navigation

enum class BottomTab(
    val route: String, val label: String
) {
    COMMUNITY("community", "社区"),
    COMMERCE("commerce", "商城"),
    AI("ai", "AI 拼图"),
    MESSAGE(
        "message",
        "消息"
    ),
    PROFILE("profile", "我的")
}

object AppRoute {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val POST_DETAIL = "post/{postId}"
    const val POST_CREATE = "post_create"
    const val IMAGE_SELECT = "image_select"
    const val AI_PARAMS = "ai_params"
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
    const val SETTINGS = "settings"

    val allRoutes = listOf(
        SPLASH,
        LOGIN,
        POST_DETAIL,
        POST_CREATE,
        IMAGE_SELECT,
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
        SETTINGS
    ) + BottomTab.entries.map { it.route }

    fun postDetail(postId: String) = "post/$postId"
    fun aiProgress(jobId: String) = "ai_progress/$jobId"
    fun patternResult(patternId: String) = "pattern/$patternId"
    fun productDetail(productId: String) = "product/$productId"
    fun paymentResult(orderId: String) = "payment_result/$orderId"
    fun conversation(conversationId: String) = "conversation/$conversationId"
}
