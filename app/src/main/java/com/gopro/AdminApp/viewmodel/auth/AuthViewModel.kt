package com.gopro.AdminApp.viewmodel.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gopro.AdminApp.data.local.TokenManager
import com.gopro.AdminApp.data.local.UserSessionManager
import com.gopro.AdminApp.model.dto.request.auth.LoginRequest
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.network.auth.AuthApi
import com.gopro.AdminApp.presentation.state.ScreenState
import com.gopro.AdminApp.utils.ApiErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.log

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val sessionManager = UserSessionManager(application)

    private val api = RetrofitClient.createService(AuthApi::class.java)

    private val _state = MutableStateFlow(ScreenState<Unit>())
    val state: StateFlow<ScreenState<Unit>> = _state.asStateFlow()

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isActionLoading = true,
                    actionSuccess = false,
                    actionErrorMessage = null,
                    fieldErrors = emptyMap()
                )
            }

            try {
                val response = api.loginUser(request)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    tokenManager.saveTokens(
                        token = loginResponse.token,
                        refreshToken = loginResponse.refreshToken ?: ""
                    )

                    sessionManager.saveSession(
                        email = loginResponse.email,
                        name = loginResponse.fullName,
                        roles = loginResponse.roles ?: emptyList(),
                        perms = loginResponse.permissions ?: emptyList(),
                        photoUrl = loginResponse.photoUrl
                    )

                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                } else {
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal mengubah data")
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            actionErrorMessage = errorResult.generalErrorMessage,
                            fieldErrors = errorResult.fieldErrors
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isActionLoading = false,
                        actionErrorMessage = "Gagal terhubung ke server: ${e.localizedMessage}"
                    )
                }
            }
        }
    }



    fun clearEmailError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Email") }
    }

    fun clearPasswordError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Password") }
    }

    fun clearGeneralError() {
        _state.update { it.copy(actionErrorMessage = null) }
    }
}