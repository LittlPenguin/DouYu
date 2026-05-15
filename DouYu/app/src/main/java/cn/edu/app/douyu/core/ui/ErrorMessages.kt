package cn.edu.app.douyu.core.ui

import cn.edu.app.douyu.core.data.ApiException
import cn.edu.app.douyu.core.network.ApiResponse

/** Maps backend ErrorCode to user-readable Chinese messages. */
object ErrorMessages {
    private val messages = mapOf(
        "UNAUTHORIZED" to "登录已过期，请重新登录",
        "FORBIDDEN" to "没有权限执行此操作",
        "NOT_FOUND" to "内容不存在",
        "INVALID_ARGUMENT" to "请求参数有误",
        "CONFLICT" to "操作冲突，请重试",
        "RATE_LIMITED" to "操作太频繁，请稍后再试",
        "AUDIT_REJECTED" to "内容审核未通过",
        "AI_TASK_FAILED" to "AI 生成失败，请重试",
        "INVENTORY_NOT_ENOUGH" to "库存不足",
        "PAYMENT_FAILED" to "支付失败，请重试",
        "INTERNAL_ERROR" to "服务器开小差了，请稍后再试"
    )

    /** Returns a user-readable message for the given error code. */
    fun forCode(code: String): String = messages[code] ?: "未知错误，请稍后再试"

    /** Extracts a user-readable message from an API response, or returns a default. */
    fun fromResponse(response: ApiResponse<*>?): String {
        if (response == null) return "网络连接失败，请检查网络后重试"
        return forCode(response.code)
    }

    /** Maps an exception to a user-readable message. */
    fun fromException(e: Exception): String = when (e) {
        is ApiException -> {
            val msg = forCode(e.code)
            if (e.traceId != null) "$msg\ntraceId: ${e.traceId}" else msg
        }
        is java.net.SocketTimeoutException -> "请求超时，请稍后再试"
        is java.net.UnknownHostException -> "网络连接失败，请检查网络"
        is java.io.IOException -> "网络异常，请稍后再试"
        else -> "加载失败: ${e.message ?: "未知错误"}"
    }

    /** Returns true if the error code indicates the user needs to re-login. */
    fun isAuthError(code: String): Boolean = code == "UNAUTHORIZED"
}
