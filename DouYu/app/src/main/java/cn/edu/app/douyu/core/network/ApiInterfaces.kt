package cn.edu.app.douyu.core.network

import cn.edu.app.douyu.core.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("/api/v1/auth/sms-code")
    suspend fun sendSmsCode(@Body request: SmsCodeRequest): ApiResponse<Unit>

    @POST("/api/v1/auth/login/sms")
    suspend fun loginBySms(@Body request: SmsLoginRequest): ApiResponse<AuthSession>
}

interface UserApi {
    @GET("/api/v1/users/me")
    suspend fun me(): ApiResponse<UserProfile>
}

interface UploadApi {
    @POST("/api/v1/uploads/presign")
    suspend fun presign(@Body request: UploadPresignRequest): ApiResponse<UploadPresign>
}

interface CommunityApi {
    @GET("/api/v1/posts/feed")
    suspend fun feed(): ApiResponse<PageResponse<Post>>

    @GET("/api/v1/posts/{postId}")
    suspend fun post(@Path("postId") postId: String): ApiResponse<Post>
}

interface PatternApi {
    @GET("/api/v1/patterns/jobs")
    suspend fun jobs(): ApiResponse<List<PatternJob>>

    @GET("/api/v1/patterns/{patternId}")
    suspend fun pattern(@Path("patternId") patternId: String): ApiResponse<PatternAsset>
}

interface ProductApi {
    @GET("/api/v1/products")
    suspend fun products(): ApiResponse<PageResponse<Product>>

    @GET("/api/v1/products/{productId}")
    suspend fun product(@Path("productId") productId: String): ApiResponse<Product>
}

interface CartApi {
    @GET("/api/v1/cart")
    suspend fun cart(): ApiResponse<Cart>
}

interface OrderApi {
    @GET("/api/v1/orders/{orderId}")
    suspend fun order(@Path("orderId") orderId: String): ApiResponse<Order>
}

interface PaymentApi {
    @POST("/api/v1/payments")
    suspend fun createPayment(@Body request: CreatePaymentRequest): ApiResponse<Payment>
}

interface MessageApi {
    @GET("/api/v1/messages/notifications")
    suspend fun notifications(): ApiResponse<List<NotificationMessage>>

    @GET("/api/v1/messages/conversations")
    suspend fun conversations(): ApiResponse<List<Conversation>>
}

interface RewardApi {
    @GET("/api/v1/rewards/me")
    suspend fun me(): ApiResponse<RewardSummary>
}
