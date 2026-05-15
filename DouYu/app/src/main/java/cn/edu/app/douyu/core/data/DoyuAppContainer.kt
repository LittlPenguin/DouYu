package cn.edu.app.douyu.core.data

import cn.edu.app.douyu.core.network.*

object DoyuAppContainer {
    private val tokenStore = InMemoryTokenStore()

    val apiClient = DoyuApiClient(
        baseUrl = "http://10.0.2.2:8080",
        tokenProvider = tokenStore,
        refreshTokenProvider = tokenStore,
        onTokenRefreshed = { pair -> tokenStore.save(pair) },
        onAuthExpired = { tokenStore.clear() }
    )

    val authSessionManager = AuthSessionManager(apiClient.authApi, tokenStore)

    val uploadTransport: UploadTransport = OkHttpUploadTransport()
    val patternGenerationWorkflow = PatternGenerationWorkflow(
        apiClient.uploadApi, apiClient.patternApi, uploadTransport
    )

    val communityRepository: CommunityRepository = RealCommunityRepository(apiClient.communityApi)
    val patternRepository: PatternRepository = RealPatternRepository(apiClient.patternApi)
    val commerceRepository: CommerceRepository = RealCommerceRepository(
        apiClient.productApi, apiClient.cartApi, apiClient.orderApi, apiClient.paymentApi
    )
    val messageRepository: MessageRepository = RealMessageRepository(apiClient.messageApi)
    val profileRepository: ProfileRepository = RealProfileRepository(
        apiClient.userApi, apiClient.rewardApi, apiClient.patternApi
    )
}
