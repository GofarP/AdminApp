package com.gopro.AdminApp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gopro.AdminApp.data.local.UserSessionManager
import com.gopro.AdminApp.model.dto.request.PasswordUpdateRequest
import com.gopro.AdminApp.model.dto.response.ProfileResponse
import com.gopro.AdminApp.network.ProfileApi
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.presentation.state.ScreenState
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.utils.ApiErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.createService(ProfileApi::class.java)
    private val sessionManager = UserSessionManager(application)

    private val _state = MutableStateFlow(ScreenState<ProfileResponse>())
    val state: StateFlow<ScreenState<ProfileResponse>> = _state.asStateFlow()

    val userPhoto: StateFlow<String?> = sessionManager.userPhotoFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }

            try {
                val response = api.getProfile()

                if (response.isSuccessful && response.body() != null) {
                    val profileData = response.body()!!
                    _state.update { it.copy(dataState = UiState.Success(profileData)) }

                    profileData.fullName?.let { sessionManager.saveUserName(it) }
                    profileData.photoUrl?.let { sessionManager.saveUserPhoto(it) }
                } else {
                    _state.update { it.copy(dataState = UiState.Error("Gagal mengambil data profil: ${response.code()}")) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(dataState = UiState.Error("Gagal terhubung ke server")) }
            }
        }
    }

    fun updateProfile(fullName: String, email: String, photoFile: File?) {
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
                val fullNamePart = MultipartBody.Part.createFormData("FullName", fullName)
                val emailPart = MultipartBody.Part.createFormData("Email", email)

                val photoPart = photoFile?.let { file ->
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("Photo", file.name, requestBody)
                }

                val response = api.updateProfile(fullNamePart, emailPart, photoPart)

                if (response.isSuccessful) {
                    val updatedData = response.body()

                    // Langsung tembak ke Store agar UI (foto/nama) berubah seketika tanpa nunggu loading
                    if (updatedData != null) {
                        updatedData.fullName?.let { sessionManager.saveUserName(it) }
                        updatedData.photoUrl?.let { sessionManager.saveUserPhoto(it) }
                    } else {
                        sessionManager.saveUserName(fullName)
                    }

                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    // Tarik ulang dari server untuk memastikan konsistensi
                    fetchProfile()
                } else {
                    // Mengembalikan sistem error handling standar (ApiErrorHandler)
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal mengubah profil")
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

    fun updatePassword(request: PasswordUpdateRequest) {
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
                val response = api.updatePassword(request)
                if (response.isSuccessful) {
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                } else {
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal mengubah password")
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

    fun clearFullNameError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "FullName") }
    }

    fun clearEmailError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Email") }
    }

    fun clearOldPasswordError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "OldPassword") }
    }

    fun clearNewPasswordError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "NewPassword") }
    }

    fun clearGeneralError() {
        _state.update { it.copy(actionErrorMessage = null) }
    }

    fun resetActionState() {
        _state.update { it.copy(actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap()) }
    }
}