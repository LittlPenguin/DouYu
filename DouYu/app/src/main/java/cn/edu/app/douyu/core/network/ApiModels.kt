package cn.edu.app.douyu.core.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T? = null,
    val traceId: String
) {
    val isOk: Boolean
        get() = code == "OK"
}

@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val total: Int,
    val hasMore: Boolean
)

data class ApiError(
    val code: String,
    val message: String,
    val traceId: String? = null
)

object ApiHeaders {
    const val AUTHORIZATION = "Authorization"
    const val IDEMPOTENCY_KEY = "Idempotency-Key"
    const val REQUEST_ID = "X-Request-Id"
}
