package cn.edu.app.douyu.core.network

import cn.edu.app.douyu.core.model.AuthSession
import cn.edu.app.douyu.core.model.RefreshTokenRequest
import cn.edu.app.douyu.core.model.SmsLoginRequest
import cn.edu.app.douyu.core.model.TokenPair

class AuthSessionManager(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) {
    suspend fun loginBySms(request: SmsLoginRequest): AuthSession {
        val response = authApi.loginBySms(request)
        val session = requireSuccess(response)
        tokenStore.save(session)
        return session
    }

    suspend fun refresh(): TokenPair {
        val refreshToken = tokenStore.refreshToken().orEmpty()
        if (refreshToken.isBlank()) {
            tokenStore.clear()
            throw IllegalStateException("Missing refresh token")
        }
        val response = authApi.refresh(RefreshTokenRequest(refreshToken))
        val tokenPair = requireSuccess(response)
        tokenStore.save(tokenPair)
        return tokenPair
    }

    suspend fun logout() {
        runCatching { authApi.logout() }
        tokenStore.clear()
    }

    private fun <T> requireSuccess(response: ApiResponse<T>): T {
        val data = response.data
        if (!response.isOk || data == null) {
            throw IllegalStateException("API error ${response.code}: ${response.message}")
        }
        return data
    }
}
