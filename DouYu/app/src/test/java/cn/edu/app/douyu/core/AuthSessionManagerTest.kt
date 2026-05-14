package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.MockData
import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.network.ApiResponse
import cn.edu.app.douyu.core.network.AuthApi
import cn.edu.app.douyu.core.network.AuthSessionManager
import cn.edu.app.douyu.core.network.InMemoryTokenStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthSessionManagerTest {
    @Test
    fun loginRefreshAndLogoutPersistTokenState() = runBlocking {
        val authApi = FakeAuthApi()
        val tokenStore = InMemoryTokenStore()
        val manager = AuthSessionManager(authApi, tokenStore)

        manager.loginBySms(SmsLoginRequest("13800000000", "123456"))
        assertEquals("access_login", tokenStore.accessToken())
        assertEquals("refresh_login", tokenStore.refreshToken())

        manager.refresh()
        assertEquals("access_refreshed", tokenStore.accessToken())
        assertEquals("refresh_refreshed", tokenStore.refreshToken())

        manager.logout()
        assertNull(tokenStore.accessToken())
        assertNull(tokenStore.refreshToken())
    }

    private class FakeAuthApi : AuthApi {
        override suspend fun sendSmsCode(request: SmsCodeRequest): ApiResponse<Unit> =
            ApiResponse("OK", "success", Unit, "trace_sms")

        override suspend fun loginBySms(request: SmsLoginRequest): ApiResponse<AuthSession> =
            ApiResponse(
                "OK",
                "success",
                AuthSession("access_login", "refresh_login", 3600, MockData.user),
                "trace_login"
            )

        override suspend fun refresh(request: RefreshTokenRequest): ApiResponse<TokenPair> =
            ApiResponse("OK", "success", TokenPair("access_refreshed", "refresh_refreshed", 3600), "trace_refresh")

        override suspend fun logout(): ApiResponse<Unit> =
            ApiResponse("OK", "success", Unit, "trace_logout")
    }
}
