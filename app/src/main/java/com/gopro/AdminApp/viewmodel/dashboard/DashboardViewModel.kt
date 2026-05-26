package com.gopro.AdminApp.viewmodel.dashboard

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gopro.AdminApp.data.local.TokenManager
import com.gopro.AdminApp.data.local.UserSessionManager
import com.gopro.AdminApp.model.dto.request.auth.LogoutRequest
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.network.auth.AuthApi
import com.gopro.AdminApp.presentation.state.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = UserSessionManager(application)
    private val tokenManager= TokenManager(application)
    private val api = RetrofitClient.createService(AuthApi::class.java)

    private val _state= MutableStateFlow(ScreenState<Unit>())
    val state=_state.asStateFlow()
    val userName: StateFlow<String> = sessionManager.userNameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Guest"
        )

    val userPhoto: StateFlow<String?> = sessionManager.userPhotoFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            val savedRefreshToken=tokenManager.getRefreshToken()
            if(!savedRefreshToken.isNullOrEmpty()){
                try{
                    val request = LogoutRequest(refreshToken = savedRefreshToken)
                    val response=api.logoutUser(request)

                    if(!response.isSuccessful){
                        println("Backend logout failed: ${response.code()}")
                    }
                }catch (e: Exception){
                    println("Network error during logout: ${e.localizedMessage}")
                }
            }

            sessionManager.clear()
            tokenManager.clear()
            _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
            onLogoutComplete()
        }
    }

}