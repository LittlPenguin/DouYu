package cn.edu.app.douyu.core.ui

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
    data object RequireLogin : UiState<Nothing>
    data object Forbidden : UiState<Nothing>
    data object Reviewing : UiState<Nothing>
    data object WeakNetwork : UiState<Nothing>
}
