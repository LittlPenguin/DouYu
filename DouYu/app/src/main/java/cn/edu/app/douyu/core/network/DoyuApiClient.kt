package cn.edu.app.douyu.core.network

import cn.edu.app.douyu.core.model.RefreshTokenRequest
import cn.edu.app.douyu.core.model.TokenPair
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import java.util.UUID
import java.util.concurrent.TimeUnit

class AuthInterceptor(
    private val tokenProvider: AccessTokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenProvider.accessToken()
        val authenticatedRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .header(ApiHeaders.AUTHORIZATION, "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(authenticatedRequest)
    }
}

class IdempotencyKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val needsKey = request.method in IDEMPOTENT_SUBMISSION_METHODS &&
            request.header(ApiHeaders.IDEMPOTENCY_KEY).isNullOrBlank()
        val next = if (needsKey) {
            request.newBuilder()
                .header(ApiHeaders.IDEMPOTENCY_KEY, UUID.randomUUID().toString())
                .build()
        } else {
            request
        }
        return chain.proceed(next)
    }

    private companion object {
        val IDEMPOTENT_SUBMISSION_METHODS = setOf("POST", "PATCH", "DELETE")
    }
}

class TokenRefreshAuthenticator(
    private val refreshTokenProvider: RefreshTokenProvider,
    private val onTokenRefreshed: suspend (TokenPair) -> Unit,
    private val onRefreshFailed: suspend () -> Unit,
    private val refreshServiceFactory: () -> AuthApi
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        if (response.request.url.encodedPath.endsWith("/api/v1/auth/refresh")) return null

        val refreshToken = refreshTokenProvider.refreshToken().orEmpty()
        if (refreshToken.isBlank()) {
            runBlocking { onRefreshFailed() }
            return null
        }

        val refreshed = runBlocking {
            runCatching {
                refreshServiceFactory().refresh(RefreshTokenRequest(refreshToken))
            }.getOrNull()
        }

        val tokenPair = refreshed?.data
        return if (refreshed?.isOk == true && tokenPair != null) {
            runBlocking { onTokenRefreshed(tokenPair) }
            response.request.newBuilder()
                .header(ApiHeaders.AUTHORIZATION, "Bearer ${tokenPair.accessToken}")
                .build()
        } else {
            runBlocking { onRefreshFailed() }
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

class DoyuApiClient(
    baseUrl: String,
    tokenProvider: AccessTokenProvider,
    refreshTokenProvider: RefreshTokenProvider = object : RefreshTokenProvider {
        override fun refreshToken(): String? = null
    },
    private val onTokenRefreshed: suspend (TokenPair) -> Unit = {},
    private val onAuthExpired: suspend () -> Unit = {},
    okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
) {
    private val refreshRetrofit: Retrofit
    private val retrofit: Retrofit

    init {
        val refreshClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        refreshRetrofit = createRetrofit(baseUrl, refreshClient)

        val client = okHttpClientBuilder
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(IdempotencyKeyInterceptor())
            .authenticator(
                TokenRefreshAuthenticator(
                    refreshTokenProvider = refreshTokenProvider,
                    onTokenRefreshed = onTokenRefreshed,
                    onRefreshFailed = onAuthExpired,
                    refreshServiceFactory = { refreshRetrofit.create(AuthApi::class.java) }
                )
            )
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        retrofit = createRetrofit(baseUrl, client)
    }

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val uploadApi: UploadApi = retrofit.create(UploadApi::class.java)
    val communityApi: CommunityApi = retrofit.create(CommunityApi::class.java)
    val patternApi: PatternApi = retrofit.create(PatternApi::class.java)
    val productApi: ProductApi = retrofit.create(ProductApi::class.java)
    val cartApi: CartApi = retrofit.create(CartApi::class.java)
    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
    val paymentApi: PaymentApi = retrofit.create(PaymentApi::class.java)
    val messageApi: MessageApi = retrofit.create(MessageApi::class.java)
    val rewardApi: RewardApi = retrofit.create(RewardApi::class.java)

    private fun createRetrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(DoyuJson.asConverterFactory(DoyuJsonMediaType))
            .build()
}
