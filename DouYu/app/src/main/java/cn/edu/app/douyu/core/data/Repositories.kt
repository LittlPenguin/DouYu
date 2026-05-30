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
    fun comments(postId: String): PageResponse<Comment>
    fun createPost(request: CreatePostRequest): Post
    fun createComment(postId: String, request: CreateCommentRequest): Comment
    fun likePost(postId: String): PostInteractionResult
    fun unlikePost(postId: String): PostInteractionResult
    fun favoritePost(postId: String): PostInteractionResult
    fun unfavoritePost(postId: String): PostInteractionResult
}

interface PatternRepository {
    fun featuredJob(): PatternJob
    fun history(): PageResponse<PatternJob>
    fun job(jobId: String): PatternJob
    fun pattern(patternId: String): PatternAsset
}

interface CommerceRepository {
    fun products(): PageResponse<Product>
    fun productsByCategory(categoryId: String): PageResponse<Product>
    fun product(productId: String): Product
    fun cart(): Cart
    fun addItemToCart(productId: String, skuId: String, quantity: Int): Cart
    fun updateCartItem(itemId: String, quantity: Int): Cart
    fun removeCartItem(itemId: String): Cart
    fun orders(): PageResponse<Order>
    fun order(): Order
    fun order(orderId: String): Order
    fun createOrder(itemIds: List<String>, addressId: String): Order
    fun createPayment(orderId: String, channel: PaymentChannel): Payment
    fun paymentStatus(paymentId: String): Payment
}

interface MessageRepository {
    fun notifications(): PageResponse<NotificationMessage>
    fun conversations(): PageResponse<Conversation>
    fun chat(conversationId: String): PageResponse<ChatMessage>
}

interface ProfileRepository {
    fun dashboard(): DashboardData
    fun patterns(): List<PatternAsset>
    fun favorites(): PageResponse<PatternAsset>
    fun checkinStatus(): CheckinStatus
    fun badges(): List<Badge>
}

object MockData {
    val user = UserProfile(
        userId = "user_001",
        nickname = "栗子拼豆",
        avatarUrl = null,
        bio = "喜欢把日常小灵感拼成挂件和冰箱贴。",
        level = 6,
        isMinor = false,
        followingCount = 38,
        followerCount = 1260
    )

    private val guest = UserProfile(
        userId = "user_002",
        nickname = "小岛手作",
        avatarUrl = null,
        bio = "新手友好图纸收集中。",
        level = 4,
        isMinor = false,
        followingCount = 21,
        followerCount = 480
    )

    val confirmedAiInput = FileAsset(
        fileId = "file_ai_input_001",
        ownerId = user.userId,
        usage = UploadUsage.AI_INPUT,
        storageKey = "ai-input/2026/05/cat-photo.jpg",
        mimeType = "image/jpeg",
        sizeBytes = 612_000,
        width = 1280,
        height = 960,
        auditStatus = AuditStatus.PASS,
        publicUrl = null
    )

    val posts = listOf(
        Post(
            postId = "post_001",
            authorId = user.userId,
            author = user,
            title = "把猫猫照片拼成了 5cm 小挂件",
            content = "用新手模式降到 12 色，边缘比直接像素化清楚很多，材料清单也刚好能一键凑齐。",
            mediaFileIds = listOf("file_post_001"),
            mediaColors = listOf(0xFFF6A6B2, 0xFFFFD7C2, 0xFFA8DADC),
            topicIds = listOf("topic_pet"),
            topicNames = listOf("宠物拼豆"),
            linkedPatternId = "pattern_001",
            status = ContentStatus.VISIBLE,
            likeCount = 238,
            favoriteCount = 92,
            commentCount = 31,
            coverImageUrl = "http://10.0.2.2:8081/seed/community/newbie-guide.jpg"
        ),
        Post(
            postId = "post_002",
            authorId = guest.userId,
            author = guest,
            title = "新手第一套色卡怎么选",
            content = "如果只做头像和小挂件，先买常用肤色、黑白灰、低饱和粉蓝绿就够了。",
            mediaFileIds = listOf("file_post_002"),
            mediaColors = listOf(0xFFFDF1F3, 0xFFBEE7D5, 0xFFC7D9FF),
            topicIds = listOf("topic_beginner"),
            topicNames = listOf("新手教程"),
            linkedPatternId = null,
            status = ContentStatus.VISIBLE,
            likeCount = 96,
            favoriteCount = 44,
            commentCount = 12,
            coverImageUrl = "http://10.0.2.2:8081/seed/community/color-palette.jpg"
        )
    )

    val colors = listOf(
        PaletteColorCount("A01", "奶油白", 0xFFFFF7EA, 248, "sku_bead_a01"),
        PaletteColorCount("P12", "豆沙粉", 0xFFF3A7B5, 136, "sku_bead_p12"),
        PaletteColorCount("G08", "薄荷绿", 0xFFA9DCC5, 82, "sku_bead_g08"),
        PaletteColorCount("B06", "雾蓝", 0xFF9FC7EA, 64, "sku_bead_b06")
    )

    val patternMaterials = PatternMaterials(
        totalBeads = 530,
        colors = listOf(
            PatternColorEntry("A01", "奶油白", 248, "sku_bead_a01"),
            PatternColorEntry("P12", "豆沙粉", 136, "sku_bead_p12"),
            PatternColorEntry("G08", "薄荷绿", 82, "sku_bead_g08"),
            PatternColorEntry("B06", "雾蓝", 64, "sku_bead_b06")
        )
    )

    val patterns = listOf(
        PatternAsset(
            patternId = "pattern_001",
            jobId = "job_002",
            ownerId = user.userId,
            title = "猫猫头像小挂件",
            previewFileId = "file_pattern_preview_001",
            gridFileId = "file_pattern_grid_001",
            colorMapFileId = "file_pattern_color_001",
            pdfFileId = "file_pattern_pdf_001",
            beadSize = BeadSize.MM_2_6,
            widthCells = 32,
            heightCells = 32,
            totalBeads = 530,
            paletteName = "豆屿通用 48 色",
            status = ContentStatus.VISIBLE,
            colorStats = colors,
            materials = patternMaterials
        ),
        PatternAsset(
            patternId = "pattern_002",
            jobId = "job_003",
            ownerId = user.userId,
            title = "生日小花束杯垫",
            previewFileId = "file_pattern_preview_002",
            gridFileId = "file_pattern_grid_002",
            colorMapFileId = "file_pattern_color_002",
            pdfFileId = null,
            beadSize = BeadSize.MM_5,
            widthCells = 28,
            heightCells = 28,
            totalBeads = 412,
            paletteName = "新手低色数",
            status = ContentStatus.VISIBLE,
            colorStats = colors.take(3),
            materials = PatternMaterials(412, patternMaterials.colors.take(3))
        )
    )

    val jobs = listOf(
        PatternJob(
            jobId = "job_001",
            userId = user.userId,
            inputFileId = confirmedAiInput.fileId,
            inputName = "cat_photo.jpg",
            beadSize = BeadSize.MM_2_6,
            targetSize = "SMALL_CHARM",
            difficulty = PatternDifficulty.BEGINNER,
            paletteId = "palette_doyu_48",
            paletteName = "豆屿通用 48 色",
            style = PatternStyle.CUTE,
            status = PatternJobStatus.PROCESSING,
            progress = 62,
            failureReason = null,
            patternId = null
        ),
        PatternJob(
            jobId = "job_002",
            userId = user.userId,
            inputFileId = "file_ai_input_002",
            inputName = "flower.png",
            beadSize = BeadSize.MM_5,
            targetSize = "MEDIUM_DECOR",
            difficulty = PatternDifficulty.NORMAL,
            paletteId = "palette_beginner_low",
            paletteName = "新手低色数",
            style = PatternStyle.RESTORE,
            status = PatternJobStatus.SUCCEEDED,
            progress = 100,
            failureReason = null,
            patternId = "pattern_002"
        ),
        PatternJob(
            jobId = "job_003",
            userId = user.userId,
            inputFileId = "file_ai_input_003",
            inputName = "too_dark.jpg",
            beadSize = BeadSize.MM_2_6,
            targetSize = "SMALL_CHARM",
            difficulty = PatternDifficulty.BEGINNER,
            paletteId = "palette_doyu_48",
            paletteName = "豆屿通用 48 色",
            style = PatternStyle.CUTE,
            status = PatternJobStatus.FAILED,
            progress = 0,
            failureReason = "图片不清晰或主体不明确",
            patternId = null
        )
    )

    private fun sku(
        skuId: String,
        productId: String,
        specName: String,
        priceCent: Int,
        stock: Int
    ) = ProductSku(skuId, productId, specName, priceCent, stock, SkuStatus.ON_SALE)

    val products = listOf(
        Product(
            productId = "product_001",
            type = ProductType.SELF_OPERATED,
            sellerId = null,
            title = "新手 2.6mm 拼豆入门包",
            description = "含常用色、透明板、离型纸和基础教程。",
            categoryId = "cat_beginner",
            categoryName = "新手套装",
            status = ProductStatus.ON_SALE,
            auditStatus = AuditStatus.PASS,
            skus = listOf(sku("sku_001_basic", "product_001", "2.6mm 入门套装", 6990, 128)),
            swatchColor = 0xFFF6A6B2,
            imageUrl = "http://10.0.2.2:8081/seed/commerce/starter-kit.jpg"
        ),
        Product(
            productId = "product_002",
            type = ProductType.SELF_OPERATED,
            sellerId = null,
            title = "豆屿通用 48 色色卡",
            description = "适合头像、小挂件、礼物图案的低饱和色组。",
            categoryId = "cat_palette",
            categoryName = "色卡",
            status = ProductStatus.ON_SALE,
            auditStatus = AuditStatus.PASS,
            skus = listOf(sku("sku_002_palette", "product_002", "48 色套组", 4590, 76)),
            swatchColor = 0xFFA8DADC,
            imageUrl = "http://10.0.2.2:8081/seed/commerce/bead-white.jpg"
        ),
        Product(
            productId = "product_003",
            type = ProductType.PLAYER_CUSTOM_SERVICE,
            sellerId = "user_002",
            title = "宠物头像定制咨询",
            description = "玩家直连沟通，平台保留举报与订单线索。",
            categoryId = "cat_custom",
            categoryName = "玩家定制",
            status = ProductStatus.ON_SALE,
            auditStatus = AuditStatus.PASS,
            skus = listOf(sku("sku_003_custom", "product_003", "咨询定金", 0, 1)),
            swatchColor = 0xFFFFD7C2,
            imageUrl = "http://10.0.2.2:8081/seed/commerce/player-custom-service.jpg"
        )
    )

    val cart = Cart(
        items = products.take(2).mapIndexed { index, product ->
            val sku = product.skus.first()
            CartItem(
                itemId = "cart_item_${index + 1}",
                productId = product.productId,
                product = CartProductSummary(
                    title = product.title,
                    imageUrl = product.imageUrl,
                    swatchColor = product.swatchColor
                ),
                sku = sku,
                quantity = index + 1
            )
        }
    )

    val order = Order(
        orderId = "order_001",
        buyerId = user.userId,
        sellerType = SellerType.PLATFORM,
        sellerId = null,
        orderType = OrderType.SELF_OPERATED,
        status = OrderStatus.WAITING_PAYMENT,
        totalAmountCent = cart.payableAmountCent,
        payableAmountCent = cart.payableAmountCent,
        items = cart.items.mapIndexed { index, item ->
            OrderItem(
                orderItemId = "order_item_${index + 1}",
                productId = item.productId ?: "",
                skuId = item.sku?.skuId ?: "",
                title = item.product?.title ?: "",
                specName = item.sku?.specName ?: "",
                priceCent = item.sku?.priceCent ?: 0,
                quantity = item.quantity
            )
        },
        addressSnapshot = AddressSnapshot(addressId = "mock_address_001")
    )

    val payment = Payment(
        paymentId = "payment_001",
        orderId = order.orderId,
        channel = PaymentChannel.WECHAT_APP,
        status = PaymentStatus.PROCESSING,
        amountCent = order.payableAmountCent,
        payParams = mapOf("stub" to "true", "nonce" to "mock-payment-param")
    )

    val notifications = listOf(
        NotificationMessage("notice_001", NotificationType.AI_TASK, "AI 图纸已进入处理中", "预计 2 分钟内完成，完成后会通知你。", unread = true),
        NotificationMessage("notice_002", NotificationType.FAVORITE, "你的帖子有新收藏", "猫猫小挂件被 12 位同好收藏。", unread = false)
    )

    val conversations = listOf(
        Conversation("conv_001", "user_001", "user_002"),
        Conversation("conv_002", "user_001", "support_001")
    )

    val badges = listOf(
        Badge("badge_001", "连续签到 7 天", "保持创作节奏", true),
        Badge("badge_002", "第一张 AI 图纸", "完成首次图片转拼豆", true),
        Badge("badge_003", "社区种草官", "作品获得 100 次收藏", false)
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

    override fun post(postId: String): Post = MockData.posts.firstOrNull { it.postId == postId } ?: MockData.posts.first()

    override fun comments(postId: String): PageResponse<Comment> {
        val items = listOf(
            Comment("comment_001", postId, MockData.user.userId, MockData.user, null, "这个色号清单太适合新手了。", ContentStatus.VISIBLE),
            Comment(
                "comment_002",
                postId,
                "user_003",
                MockData.user.copy(userId = "user_003", nickname = "豆豆"),
                null,
                "想看 5mm 版本。",
                ContentStatus.VISIBLE
            )
        )
        return PageResponse(items, 1, 20, items.size, false)
    }

    override fun createPost(request: CreatePostRequest): Post =
        MockData.posts.first().copy(
            postId = "post_preview_created",
            title = request.title,
            content = request.content,
            mediaFileIds = request.mediaFileIds,
            topicIds = request.topicIds,
            topicNames = emptyList(),
            linkedPatternId = request.linkedPatternId,
            status = ContentStatus.REVIEWING,
            likeCount = 0,
            favoriteCount = 0,
            commentCount = 0
        )

    override fun createComment(postId: String, request: CreateCommentRequest): Comment =
        Comment(
            commentId = "comment_preview_created",
            postId = postId,
            authorId = MockData.user.userId,
            author = MockData.user,
            parentId = request.parentId,
            content = request.content,
            status = ContentStatus.REVIEWING
        )

    override fun likePost(postId: String): PostInteractionResult = PostInteractionResult(liked = true)

    override fun unlikePost(postId: String): PostInteractionResult = PostInteractionResult(liked = false)

    override fun favoritePost(postId: String): PostInteractionResult = PostInteractionResult(favorited = true)

    override fun unfavoritePost(postId: String): PostInteractionResult = PostInteractionResult(favorited = false)
}

class MockPatternRepository : PatternRepository {
    override fun featuredJob(): PatternJob = MockData.jobs.first()

    override fun history(): PageResponse<PatternJob> = PageResponse(MockData.jobs, 1, 20, MockData.jobs.size, false)

    override fun job(jobId: String): PatternJob = MockData.jobs.firstOrNull { it.jobId == jobId } ?: MockData.jobs.first()

    override fun pattern(patternId: String): PatternAsset = MockData.patterns.firstOrNull { it.patternId == patternId } ?: MockData.patterns.first()
}

class MockCommerceRepository : CommerceRepository {
    override fun products(): PageResponse<Product> = PageResponse(MockData.products, 1, 20, MockData.products.size, false)

    override fun productsByCategory(categoryId: String): PageResponse<Product> {
        val filtered = MockData.products.filter { it.categoryId == categoryId }
        return PageResponse(filtered, 1, 20, filtered.size, false)
    }

    override fun product(productId: String): Product = MockData.products.firstOrNull { it.productId == productId } ?: MockData.products.first()

    override fun cart(): Cart = MockData.cart

    override fun addItemToCart(productId: String, skuId: String, quantity: Int): Cart {
        val product = MockData.products.firstOrNull { it.productId == productId }
        val sku = product?.skus?.firstOrNull { it.skuId == skuId }
        val newItem = CartItem(
            itemId = "cart_item_new_${System.currentTimeMillis()}",
            productId = productId,
            product = CartProductSummary(
                title = product?.title.orEmpty(),
                swatchColor = product?.swatchColor ?: 0
            ),
            sku = sku,
            quantity = quantity
        )
        return MockData.cart.copy(items = MockData.cart.items + newItem)
    }

    override fun updateCartItem(itemId: String, quantity: Int): Cart {
        val updated = MockData.cart.items.map {
            if (it.itemId == itemId) it.copy(quantity = quantity) else it
        }
        return Cart(items = updated)
    }

    override fun removeCartItem(itemId: String): Cart {
        return Cart(items = MockData.cart.items.filter { it.itemId != itemId })
    }

    override fun orders(): PageResponse<Order> = PageResponse(listOf(MockData.order), 1, 20, 1, false)

    override fun order(): Order = MockData.order

    override fun order(orderId: String): Order = MockData.order.copy(orderId = orderId)

    override fun createOrder(itemIds: List<String>, addressId: String): Order = MockData.order

    override fun createPayment(orderId: String, channel: PaymentChannel): Payment = MockData.payment.copy(orderId = orderId)

    override fun paymentStatus(paymentId: String): Payment = MockData.payment.copy(paymentId = paymentId)
}

class MockMessageRepository : MessageRepository {
    override fun notifications(): PageResponse<NotificationMessage> =
        PageResponse(MockData.notifications, 1, 20, MockData.notifications.size, false)

    override fun conversations(): PageResponse<Conversation> =
        PageResponse(MockData.conversations, 1, 20, MockData.conversations.size, false)

    override fun chat(conversationId: String): PageResponse<ChatMessage> {
        val conversation = MockData.conversations.firstOrNull { it.conversationId == conversationId } ?: MockData.conversations.first()
        val peerId = if (conversation.userAId == MockData.user.userId) conversation.userBId else conversation.userAId
        val items = listOf(
            ChatMessage("chat_001", conversation.conversationId, peerId, "对方", "可以先发参考图，我帮你看适合什么尺寸。", false),
            ChatMessage("chat_002", conversation.conversationId, MockData.user.userId, "我", "想做成生日礼物，预算 100 左右。", true)
        )
        return PageResponse(items, 1, 20, items.size, false)
    }
}

class MockProfileRepository : ProfileRepository {
    override fun dashboard(): DashboardData = DashboardData(
        user = MockData.user,
        reward = RewardSummary(points = 680, experience = 860, levelCode = "LV6"),
        patternCount = MockData.patterns.size,
        orderCount = 3,
        badges = MockData.badges
    )

    override fun patterns(): List<PatternAsset> = MockData.patterns

    override fun favorites(): PageResponse<PatternAsset> = PageResponse(MockData.patterns, 1, 20, MockData.patterns.size, false)

    override fun checkinStatus(): CheckinStatus = CheckinStatus(checkedToday = false)

    override fun badges(): List<Badge> = MockData.badges
}
