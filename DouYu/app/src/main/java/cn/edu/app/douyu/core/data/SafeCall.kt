package cn.edu.app.douyu.core.data

import cn.edu.app.douyu.core.network.ApiResponse
import cn.edu.app.douyu.core.ui.ErrorMessages
import cn.edu.app.douyu.core.ui.UiState

/** Centralized safeCall that maps exceptions to UiState. */
inline fun <T> safeCallToState(block: () -> T): UiState<T> {
    return try {
        val result = block()
        if (result == null) {
            UiState.Empty
        } else {
            UiState.Success(result)
        }
    } catch (e: Exception) {
        val message = ErrorMessages.fromException(e)
        if (message.contains("登录")) UiState.RequireLogin else UiState.Error(message)
    }
}

/** Safe call for list data — returns Empty when the list is empty. */
inline fun <T> safeCallToList(block: () -> List<T>): UiState<List<T>> {
    return try {
        val result = block()
        if (result.isEmpty()) UiState.Empty else UiState.Success(result)
    } catch (e: Exception) {
        UiState.Error(ErrorMessages.fromException(e))
    }
}

/** Safe call that returns null on failure (for optional data). */
inline fun <T> safeCallOrNull(block: () -> T?): T? {
    return try { block() } catch (_: Exception) { null }
}

/** Extracts error info from an ApiResponse for display. */
fun <T> ApiResponse<T>.toErrorMessage(): String = ErrorMessages.fromResponse(this)
