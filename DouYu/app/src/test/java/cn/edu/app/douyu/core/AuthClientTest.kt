package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.network.AccessTokenProvider
import cn.edu.app.douyu.core.network.AuthInterceptor
import cn.edu.app.douyu.core.network.DoyuApiClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AuthClientTest {
    @Test
    fun authInterceptorAddsBearerTokenWhenPresent() {
        val interceptor = AuthInterceptor(object : AccessTokenProvider {
            override fun accessToken(): String? = "access_123"
        })
        val request = Request.Builder().url("https://api.example.test/api/v1/users/me").build()
        val chain = TestInterceptorChain(request)

        interceptor.intercept(chain)

        assertEquals("Bearer access_123", chain.request.header("Authorization"))
    }

    @Test
    fun apiClientCreatesRetrofitServices() {
        val client = DoyuApiClient(
            baseUrl = "https://api.example.test/",
            tokenProvider = object : AccessTokenProvider {
                override fun accessToken(): String? = null
            }
        )

        assertNotNull(client.authApi)
        assertNotNull(client.uploadApi)
        assertNotNull(client.patternApi)
        assertNotNull(client.paymentApi)
    }
}
