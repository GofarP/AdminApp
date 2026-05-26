package com.gopro.AdminApp.presentation.state

sealed class UiState<out T>{
    object Idle: UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

data class ScreenState<T>(
    val dataState: UiState<T> = UiState.Idle,
    val isActionLoading: Boolean=false,
    val actionSuccess: Boolean=false,
    val actionErrorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)