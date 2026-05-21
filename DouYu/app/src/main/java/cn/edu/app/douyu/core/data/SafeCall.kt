package cn.edu.app.douyu.core.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import cn.edu.app.douyu.core.network.ApiResponse
import cn.edu.app.douyu.core.ui.ErrorMessages
import cn.edu.app.douyu.core.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Centralized safeCall that maps exceptions to UiState. Non-blocking for UI thread. */
@Composable
inline fun <T> safeCallToState(
    vararg keys: Any?,
    crossinline block: () -> T
): State<UiState<T>> = produceState<UiState<T>>(UiState.Loading, keys = keys) {
    value = withContext(Dispatchers.IO) {
        try {
            val result = block()
            if (result == null) UiState.Empty
            else UiState.Success(result)
        } catch (e: Exception) {
            exceptionToUiState(e)
        }
    }
}

fun <T> exceptionToUiState(e: Exception): UiState<T> = when (e) {
    is ApiException -> when (e.code) {
        "UNAUTHORIZED" -> UiState.RequireLogin
        "FORBIDDEN" -> UiState.Forbidden
        else -> UiState.Error(ErrorMessages.fromException(e))
    }
    is java.net.SocketTimeoutException,
    is java.net.UnknownHostException,
    is java.io.IOException -> UiState.WeakNetwork
    else -> UiState.Error(ErrorMessages.fromException(e))
}

/** Safe call for list data — returns Empty when the list is empty. */
inline fun <T> safeCallToList(block: () -> List<T>): UiState<List<T>> {
    return try {
        val result = block()
        if (result.isEmpty()) UiState.Empty else UiState.Success(result)
    } catch (e: Exception) {
        exceptionToUiState(e)
    }
}

/** Safe call that returns null on failure (for optional data). */
inline fun <T> safeCallOrNull(block: () -> T?): T? {
    return try { block() } catch (_: Exception) { null }
}

/** Extracts error info from an ApiResponse for display. */
fun <T> ApiResponse<T>.toErrorMessage(): String = ErrorMessages.fromResponse(this)
