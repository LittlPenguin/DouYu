package cn.edu.app.douyu.core.network

import cn.edu.app.douyu.core.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("/api/v1/auth/sms-code")
    suspend fun sendSmsCode(@Body request: SmsCodeRequest): ApiResponse<Unit>

    @POST("/api/v1/auth/login/sms")
    suspend fun loginBySms(@Body request: SmsLoginRequest): ApiResponse<AuthSession>

    @POST("/api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): ApiResponse<TokenPair>

    @POST("/api/v1/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): ApiResponse<Unit>
}

interface UserApi {
    @GET("/api/v1/users/me")
    suspend fun me(): ApiResponse<UserProfile>
}

interface UploadApi {
    @POST("/api/v1/uploads/presign")
    suspend fun presign(@Body request: UploadPresignRequest): ApiResponse<UploadPresign>

    @POST("/api/v1/uploads/confirm")
    suspend fun confirm(@Body request: UploadConfirmRequest): ApiResponse<FileAsset>
}

interface CommunityApi {
    @GET("/api/v1/posts/feed")
    suspend fun feed(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<Post>>

    @GET("/api/v1/posts/following")
    suspend fun following(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<Post>>

    @POST("/api/v1/posts")
    suspend fun createPost(@Body request: CreatePostRequest): ApiResponse<Post>

    @GET("/api/v1/posts/{postId}")
    suspend fun post(@Path("postId") postId: String): ApiResponse<Post>

    @POST("/api/v1/posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: String): ApiResponse<PostInteractionResult>

    @DELETE("/api/v1/posts/{postId}/like")
    suspend fun unlikePost(@Path("postId") postId: String): ApiResponse<PostInteractionResult>

    @POST("/api/v1/posts/{postId}/favorite")
    suspend fun favoritePost(@Path("postId") postId: String): ApiResponse<PostInteractionResult>

    @DELETE("/api/v1/posts/{postId}/favorite")
    suspend fun unfavoritePost(@Path("postId") postId: String): ApiResponse<PostInteractionResult>

    @GET("/api/v1/posts/{postId}/comments")
    suspend fun comments(
        @Path("postId") postId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<Comment>>

    @POST("/api/v1/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: String,
        @Body request: CreateCommentRequest
    ): ApiResponse<Comment>
}

interface PatternApi {
    @POST("/api/v1/patterns/jobs")
    suspend fun createJob(@Body request: CreatePatternJobRequest): ApiResponse<PatternJob>

    @GET("/api/v1/patterns/jobs/{jobId}")
    suspend fun job(@Path("jobId") jobId: String): ApiResponse<PatternJob>

    @GET("/api/v1/patterns/jobs")
    suspend fun jobs(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<PatternJob>>

    @POST("/api/v1/patterns/jobs/{jobId}/cancel")
    suspend fun cancelJob(@Path("jobId") jobId: String): ApiResponse<PatternJob>

    @POST("/api/v1/patterns/{patternId}/favorite")
    suspend fun favoritePattern(@Path("patternId") patternId: String): ApiResponse<Unit>

    @GET("/api/v1/patterns/favorites")
    suspend fun favorites(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<PatternAsset>>

    @GET("/api/v1/patterns/{patternId}")
    suspend fun pattern(@Path("patternId") patternId: String): ApiResponse<PatternAsset>
}

interface ProductApi {
    @GET("/api/v1/products")
    suspend fun products(
        @Query("categoryId") categoryId: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<Product>>

    @GET("/api/v1/products/{productId}")
    suspend fun product(@Path("productId") productId: String): ApiResponse<Product>
}

interface CartApi {
    @GET("/api/v1/cart")
    suspend fun cart(): ApiResponse<Cart>

    @POST("/api/v1/cart/items")
    suspend fun addItem(@Body request: AddCartItemRequest): ApiResponse<Cart>

    @PATCH("/api/v1/cart/items/{itemId}")
    suspend fun updateItem(
        @Path("itemId") itemId: String,
        @Body request: UpdateCartItemRequest
    ): ApiResponse<Cart>

    @DELETE("/api/v1/cart/items/{itemId}")
    suspend fun removeItem(@Path("itemId") itemId: String): ApiResponse<Cart>
}

interface OrderApi {
    @POST("/api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): ApiResponse<Order>

    @GET("/api/v1/orders")
    suspend fun orders(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<Order>>

    @GET("/api/v1/orders/{orderId}")
    suspend fun order(@Path("orderId") orderId: String): ApiResponse<Order>

    @POST("/api/v1/orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: String): ApiResponse<Order>
}

interface PaymentApi {
    @POST("/api/v1/payments")
    suspend fun createPayment(@Body request: CreatePaymentRequest): ApiResponse<Payment>

    @GET("/api/v1/payments/{paymentId}")
    suspend fun payment(@Path("paymentId") paymentId: String): ApiResponse<Payment>
}

interface MessageApi {
    @GET("/api/v1/messages/notifications")
    suspend fun notifications(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<NotificationMessage>>

    @POST("/api/v1/messages/notifications/read")
    suspend fun markNotificationsRead(@Body request: MarkNotificationsReadRequest): ApiResponse<Unit>

    @GET("/api/v1/messages/conversations")
    suspend fun conversations(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<Conversation>>

    @GET("/api/v1/messages/conversations/{conversationId}")
    suspend fun conversation(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<ConversationDetail>

    @POST("/api/v1/messages/conversations/{conversationId}")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body request: SendMessageRequest
    ): ApiResponse<ChatMessage>
}

interface RewardApi {
    @POST("/api/v1/checkins")
    suspend fun checkin(): ApiResponse<CheckinStatus>

    @GET("/api/v1/checkins/status")
    suspend fun checkinStatus(): ApiResponse<CheckinStatus>

    @GET("/api/v1/rewards/me")
    suspend fun me(): ApiResponse<RewardSummary>

    @GET("/api/v1/badges/me")
    suspend fun badges(): ApiResponse<BadgeListWrapper>
}
