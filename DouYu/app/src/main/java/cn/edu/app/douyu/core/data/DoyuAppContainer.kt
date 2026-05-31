package cn.edu.app.douyu.core.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import cn.edu.app.douyu.BuildConfig
import cn.edu.app.douyu.core.network.*

private val Context.doyuAuthTokenDataStore by preferencesDataStore(name = "doyu_auth_tokens")

object DoyuAppContainer {
    private val tokenStore = SwitchableTokenStore()
    @Volatile
    private var persistentTokenStoreHydrated = false

    suspend fun hydrateTokenStore(context: Context? = null) {
        val appContext = context?.applicationContext ?: return
        if (persistentTokenStoreHydrated) return

        val persistentStore = DataStoreTokenStore(appContext.doyuAuthTokenDataStore)
        persistentStore.hydrate()
        if (!persistentTokenStoreHydrated) {
            tokenStore.switchTo(persistentStore)
            persistentTokenStoreHydrated = true
        }
    }

    val isLoggedIn: Boolean get() = tokenStore.accessToken() != null

    val apiClient = DoyuApiClient(
        baseUrl = BuildConfig.API_BASE_URL,
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

    val communityRepository: CommunityRepository = RealCommunityRepository(apiClient.communityApi, apiClient.userApi)
    val patternRepository: PatternRepository = RealPatternRepository(apiClient.patternApi)
    val commerceRepository: CommerceRepository = RealCommerceRepository(
        apiClient.productApi, apiClient.cartApi, apiClient.orderApi, apiClient.paymentApi
    )
    val messageRepository: MessageRepository = RealMessageRepository(apiClient.messageApi)
    val profileRepository: ProfileRepository = RealProfileRepository(
        apiClient.userApi, apiClient.rewardApi, apiClient.patternApi
    )
}
