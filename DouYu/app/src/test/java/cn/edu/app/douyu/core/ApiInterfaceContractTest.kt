package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.ApiException
import cn.edu.app.douyu.core.data.exceptionToUiState
import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.network.*
import cn.edu.app.douyu.core.ui.UiState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import java.net.UnknownHostException

class ApiInterfaceContractTest {
    @Test
    fun authUploadAndPatternApisExposeIntegrationEndpoints() {
        assertPost(method<AuthApi>("refresh"), "/api/v1/auth/refresh")
        assertPost(method<AuthApi>("logout"), "/api/v1/auth/logout")
        assertPost(method<UploadApi>("confirm"), "/api/v1/uploads/confirm")
        assertPost(method<PatternApi>("createJob"), "/api/v1/patterns/jobs")
        assertGet(method<PatternApi>("job"), "/api/v1/patterns/jobs/{jobId}")
        assertPost(method<PatternApi>("cancelJob"), "/api/v1/patterns/jobs/{jobId}/cancel")
    }

    @Test
    fun communityCommercePaymentAndMessageApisExposeIntegrationEndpoints() {
        assertPost(method<CommunityApi>("createPost"), "/api/v1/posts")
        assertPost(method<CommunityApi>("likePost"), "/api/v1/posts/{postId}/like")
        assertDelete(method<CommunityApi>("unlikePost"), "/api/v1/posts/{postId}/like")
        assertPost(method<CommunityApi>("favoritePost"), "/api/v1/posts/{postId}/favorite")
        assertDelete(method<CommunityApi>("unfavoritePost"), "/api/v1/posts/{postId}/favorite")
        assertGet(method<CommunityApi>("comments"), "/api/v1/posts/{postId}/comments")
        assertPost(method<CommunityApi>("createComment"), "/api/v1/posts/{postId}/comments")

        assertPost(method<CartApi>("addItem"), "/api/v1/cart/items")
        assertPatch(method<CartApi>("updateItem"), "/api/v1/cart/items/{itemId}")
        assertDelete(method<CartApi>("removeItem"), "/api/v1/cart/items/{itemId}")

        assertPost(method<OrderApi>("createOrder"), "/api/v1/orders")
        assertGet(method<OrderApi>("orders"), "/api/v1/orders")
        assertPost(method<OrderApi>("cancelOrder"), "/api/v1/orders/{orderId}/cancel")
        assertGet(method<PaymentApi>("payment"), "/api/v1/payments/{paymentId}")

        assertPost(method<MessageApi>("markNotificationsRead"), "/api/v1/messages/notifications/read")
        assertGet(method<MessageApi>("conversation"), "/api/v1/messages/conversations/{conversationId}")
        assertPost(method<MessageApi>("sendMessage"), "/api/v1/messages/conversations/{conversationId}")

        assertPost(method<RewardApi>("checkin"), "/api/v1/checkins")
        assertGet(method<RewardApi>("checkinStatus"), "/api/v1/checkins/status")
        assertGet(method<RewardApi>("badges"), "/api/v1/badges/me")
    }

    @Test
    fun loginAndCommunityDtosMatchDocumentedJsonContract() {
        val user = DoyuJson.decodeFromString<UserProfile>(
            """
            {
              "userId": "user_contract",
              "nickname": "契约用户",
              "avatarUrl": "https://cdn.example.test/avatar.png",
              "bio": "拼豆爱好者",
              "level": 3,
              "isMinor": false,
              "followingCount": 7,
              "followerCount": 12
            }
            """.trimIndent()
        )
        assertEquals("https://cdn.example.test/avatar.png", user.avatarUrl)

        val loginJson = DoyuJson.encodeToString(SmsLoginRequest("13800000000", "123456"))
        assertTrue(loginJson.contains("\"ageGroup\":\"AGE_18_PLUS\""))

        val likeResponse = DoyuJson.decodeFromString<ApiResponse<PostInteractionResult>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": { "liked": true },
              "traceId": "trace_like"
            }
            """.trimIndent()
        )
        assertEquals(true, likeResponse.data?.liked)
        assertNull(likeResponse.data?.favorited)

        val favoriteResponse = DoyuJson.decodeFromString<ApiResponse<PostInteractionResult>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": { "favorited": false },
              "traceId": "trace_favorite"
            }
            """.trimIndent()
        )
        assertEquals(false, favoriteResponse.data?.favorited)
        assertNull(favoriteResponse.data?.liked)
    }

    @Test
    fun apiErrorCodesMapToExplicitUiStates() {
        assertEquals(
            UiState.RequireLogin,
            exceptionToUiState<Unit>(ApiException("UNAUTHORIZED", "token expired", "trace_auth"))
        )
        assertEquals(
            UiState.Forbidden,
            exceptionToUiState<Unit>(ApiException("FORBIDDEN", "forbidden", "trace_forbidden"))
        )
        assertEquals(UiState.WeakNetwork, exceptionToUiState<Unit>(UnknownHostException("offline")))

        val state = exceptionToUiState<Unit>(ApiException("NOT_FOUND", "missing", "trace_missing"))
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("trace_missing"))
    }

    private inline fun <reified T> method(name: String): java.lang.reflect.Method =
        T::class.java.declaredMethods.first { it.name == name }

    private fun assertPost(method: java.lang.reflect.Method, path: String) {
        assertEquals(path, method.getAnnotation(POST::class.java)?.value)
    }

    private fun assertGet(method: java.lang.reflect.Method, path: String) {
        assertEquals(path, method.getAnnotation(GET::class.java)?.value)
    }

    private fun assertPatch(method: java.lang.reflect.Method, path: String) {
        assertEquals(path, method.getAnnotation(PATCH::class.java)?.value)
    }

    private fun assertDelete(method: java.lang.reflect.Method, path: String) {
        assertEquals(path, method.getAnnotation(DELETE::class.java)?.value)
    }
}
