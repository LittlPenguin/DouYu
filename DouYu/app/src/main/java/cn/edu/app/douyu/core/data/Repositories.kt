package cn.edu.app.douyu.core.data

import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.network.PageResponse

interface AuthRepository {
    fun currentSession(): AuthSession?
    fun loginPreviewSession(): AuthSession
}

interface CommunityRepository {
    fun feed(): PageResponse<Post>
    fun post(postId: String): Post
    fun comments(postId: String): List<Comment>
}

interface PatternRepository {
    fun featuredJob(): PatternJob
    fun history(): List<PatternJob>
    fun pattern(patternId: String): PatternAsset
}

interface CommerceRepository {
    fun products(): PageResponse<Product>
    fun product(productId: String): Product
    fun cart(): Cart
    fun order(): Order
}

interface MessageRepository {
    fun notifications(): List<NotificationMessage>
    fun conversations(): List<Conversation>
    fun chat(conversationId: String): List<ChatMessage>
}

interface ProfileRepository {
    fun dashboard(): DashboardData
    fun patterns(): List<PatternAsset>
}

object MockData {
    val user = UserProfile(
        id = "user_001",
        nickname = "栗子拼豆",
        avatarUrl = null,
        bio = "喜欢把日常小灵感拼成挂件和冰箱贴。",
        level = 6,
        isMinor = false,
        followingCount = 38,
        followerCount = 1260
    )

    private val guest = UserProfile(
        id = "user_002",
        nickname = "小岛手作",
        avatarUrl = null,
        bio = "新手友好图纸收集中。",
        level = 4,
        isMinor = false,
        followingCount = 21,
        followerCount = 480
    )

    val posts = listOf(
        Post(
            id = "post_001",
            author = user,
            title = "把猫猫照片拼成了 5cm 小挂件",
            content = "用新手模式降到 12 色，边缘比直接像素化清楚很多，材料清单也刚好能一键凑齐。",
            mediaColors = listOf(0xFFF6A6B2, 0xFFFFD7C2, 0xFFA8DADC),
            topic = "宠物拼豆",
            linkedPatternId = "pattern_001",
            status = ContentStatus.VISIBLE,
            likeCount = 238,
            favoriteCount = 92,
            commentCount = 31
        ),
        Post(
            id = "post_002",
            author = guest,
            title = "新手第一套色卡怎么选",
            content = "如果只做头像和小挂件，先买常用肤色、黑白灰、低饱和粉蓝绿就够了。",
            mediaColors = listOf(0xFFFDF1F3, 0xFFBEE7D5, 0xFFC7D9FF),
            topic = "新手教程",
            linkedPatternId = null,
            status = ContentStatus.REVIEWING,
            likeCount = 96,
            favoriteCount = 44,
            commentCount = 12
        )
    )

    val colors = listOf(
        PaletteColorCount("A01", "奶油白", 0xFFFFF7EA, 248),
        PaletteColorCount("P12", "豆沙粉", 0xFFF3A7B5, 136),
        PaletteColorCount("G08", "薄荷绿", 0xFFA9DCC5, 82),
        PaletteColorCount("B06", "雾蓝", 0xFF9FC7EA, 64)
    )

    val materials = listOf(
        MaterialSuggestion("2.6mm 常用色补充包", "1 套", 2990, true),
        MaterialSuggestion("透明小方板", "2 块", 1280, true),
        MaterialSuggestion("离型纸", "1 包", 690, true)
    )

    val patterns = listOf(
        PatternAsset(
            id = "pattern_001",
            title = "猫猫头像小挂件",
            beadSize = BeadSize.MM_2_6,
            widthCells = 32,
            heightCells = 32,
            totalBeads = 530,
            paletteName = "豆屿通用 48 色",
            colorStats = colors,
            materials = materials
        ),
        PatternAsset(
            id = "pattern_002",
            title = "生日小花束杯垫",
            beadSize = BeadSize.MM_5,
            widthCells = 28,
            heightCells = 28,
            totalBeads = 412,
            paletteName = "新手低色数",
            colorStats = colors.take(3),
            materials = materials.take(2)
        )
    )

    val jobs = listOf(
        PatternJob("job_001", "cat_photo.jpg", BeadSize.MM_2_6, PatternDifficulty.BEGINNER, PatternStyle.CUTE, PatternJobStatus.PROCESSING, 62, null, "pattern_001"),
        PatternJob("job_002", "flower.png", BeadSize.MM_5, PatternDifficulty.NORMAL, PatternStyle.RESTORE, PatternJobStatus.SUCCEEDED, 100, null, "pattern_002")
    )

    val products = listOf(
        Product("product_001", ProductType.SELF_OPERATED, "新手 2.6mm 拼豆入门包", "含常用色、透明板、离型纸和基础教程。", "新手套装", ProductStatus.ON_SALE, 6990, 128, 0xFFF6A6B2),
        Product("product_002", ProductType.SELF_OPERATED, "豆屿通用 48 色色卡", "适合头像、小挂件、礼物图案的低饱和色组。", "色卡", ProductStatus.ON_SALE, 4590, 76, 0xFFA8DADC),
        Product("product_003", ProductType.PLAYER_CUSTOM_SERVICE, "宠物头像定制咨询", "玩家直连沟通，平台保留举报与订单线索。", "玩家定制", ProductStatus.ON_SALE, 0, 1, 0xFFFFD7C2)
    )

    val notifications = listOf(
        NotificationMessage("notice_001", "AI 图纸已进入处理中", "预计 2 分钟内完成，完成后会通知你。", true),
        NotificationMessage("notice_002", "你的帖子有新收藏", "猫猫小挂件被 12 位同好收藏。", false)
    )

    val conversations = listOf(
        Conversation("conv_001", "小岛手作", "可以先发参考图，我帮你看适合什么尺寸。", 2, "交易沟通请保留在平台内，谨慎添加外部联系方式。"),
        Conversation("conv_002", "豆屿客服", "关于账号注销和隐私问题，可以在设置页查看说明。", 0, null)
    )
}

class MockAuthRepository : AuthRepository {
    override fun currentSession(): AuthSession? = null

    override fun loginPreviewSession(): AuthSession = AuthSession(
        accessToken = "mock_access_token",
        refreshToken = "mock_refresh_token",
        expiresIn = 3600,
        user = MockData.user
    )
}

class MockCommunityRepository : CommunityRepository {
    override fun feed(): PageResponse<Post> = PageResponse(MockData.posts, 1, 20, MockData.posts.size, false)

    override fun post(postId: String): Post = MockData.posts.firstOrNull { it.id == postId } ?: MockData.posts.first()

    override fun comments(postId: String): List<Comment> = listOf(
        Comment("comment_001", MockData.user, "这个色号清单太适合新手了。", ContentStatus.VISIBLE),
        Comment("comment_002", MockData.user.copy(id = "user_003", nickname = "豆豆"), "想看 5mm 版本。", ContentStatus.VISIBLE)
    )
}

class MockPatternRepository : PatternRepository {
    override fun featuredJob(): PatternJob = MockData.jobs.first()

    override fun history(): List<PatternJob> = MockData.jobs

    override fun pattern(patternId: String): PatternAsset = MockData.patterns.firstOrNull { it.id == patternId } ?: MockData.patterns.first()
}

class MockCommerceRepository : CommerceRepository {
    override fun products(): PageResponse<Product> = PageResponse(MockData.products, 1, 20, MockData.products.size, false)

    override fun product(productId: String): Product = MockData.products.firstOrNull { it.id == productId } ?: MockData.products.first()

    override fun cart(): Cart = Cart(
        items = MockData.products.take(2).mapIndexed { index, product ->
            CartItem("cart_${index + 1}", product, index + 1)
        }
    )

    override fun order(): Order {
        val cart = cart()
        return Order("order_001", OrderStatus.WAITING_PAYMENT, cart.items, cart.payableAmountCent, "上海市 浦东新区 豆屿路 16 号")
    }
}

class MockMessageRepository : MessageRepository {
    override fun notifications(): List<NotificationMessage> = MockData.notifications

    override fun conversations(): List<Conversation> = MockData.conversations

    override fun chat(conversationId: String): List<ChatMessage> = listOf(
        ChatMessage("chat_001", "小岛手作", "可以先发参考图，我帮你看适合什么尺寸。", false),
        ChatMessage("chat_002", "我", "想做成生日礼物，预算 100 左右。", true)
    )
}

class MockProfileRepository : ProfileRepository {
    override fun dashboard(): DashboardData = DashboardData(
        user = MockData.user,
        reward = RewardSummary(level = 6, exp = 860, nextLevelExp = 1200, checkinToday = false),
        patternCount = MockData.patterns.size,
        orderCount = 3
    )

    override fun patterns(): List<PatternAsset> = MockData.patterns
}
