package com.gopro.AdminApp.viewmodel.dashboard

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gopro.AdminApp.data.local.TokenManager
import com.gopro.AdminApp.data.local.UserSessionManager
import com.gopro.AdminApp.model.dto.request.auth.LogoutRequest
import com.gopro.AdminApp.model.dto.response.stats.StatsResponse
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.network.auth.AuthApi
import com.gopro.AdminApp.network.dashboard.DashboardApi
import com.gopro.AdminApp.presentation.state.ScreenState
import com.gopro.AdminApp.presentation.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = UserSessionManager(application)
    private val tokenManager= TokenManager(application)
    private val api = RetrofitClient.createService(AuthApi::class.java)

    private val dashboardApi= RetrofitClient.createService(DashboardApi::class.java)

    private val _state= MutableStateFlow(ScreenState<StatsResponse>())
    val state=_state.asStateFlow()
    val userName: StateFlow<String> = sessionManager.userNameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Guest"
        )

    val userPhoto: StateFlow<String?> = sessionManager.userPhotoFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val userRole: StateFlow<String> = sessionManager.rolesFlow
        .map { rolesList ->
            rolesList.firstOrNull() ?: "-"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "-"
        )

    fun fetchStats(){
        viewModelScope.launch {
            _state.update {
                it.copy(dataState = UiState.Loading)
            }
            try{
                val response=dashboardApi.getStats()

                if(response.isSuccessful){
                    response.body()?.let { body ->
                        _state.update {
                            it.copy(dataState = UiState.Success(body))
                        }
                    } ?: run {
                        _state.update {
                            it.copy(dataState = UiState.Error("Data kosong"))
                        }
                    }
                }else{
                    val errorMsg = response.errorBody()?.string() ?: "Gagal memuat data statistik"
                    _state.update {
                        it.copy(dataState = UiState.Error(errorMsg))
                    }
                }
            }catch (e: Exception){
                _state.update {
                    it.copy(dataState = UiState.Error(e.localizedMessage ?: "Tidak ada koneksi internet"))
                }
            }
        }
    }
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