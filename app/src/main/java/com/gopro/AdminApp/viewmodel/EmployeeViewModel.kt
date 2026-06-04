package com.gopro.AdminApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.response.ErrorResponse
import com.gopro.AdminApp.model.entity.Employee
import com.gopro.AdminApp.model.entity.Role
import com.gopro.AdminApp.network.EmployeeApi
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.network.RoleApi
import com.gopro.AdminApp.presentation.state.ScreenState
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.utils.ApiErrorHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class EmployeeViewModel: ViewModel(){
    private val api = RetrofitClient.createService(EmployeeApi::class.java)
    private val roleApi = RetrofitClient.createService(RoleApi::class.java)

    private val _state = MutableStateFlow(ScreenState<List<Employee>>())
    val state: StateFlow<ScreenState<List<Employee>>> = _state.asStateFlow()

    private val _roles = MutableStateFlow<List<Role>>(emptyList())
    val roles: StateFlow<List<Role>> = _roles.asStateFlow()

    private val _isRoleLoading = MutableStateFlow(false)
    val isRoleLoading: StateFlow<Boolean> = _isRoleLoading.asStateFlow()

    private var searchRoleJob: Job? = null

    init {
        fetchEmployees()
        searchRoles("")
    }

    fun fetchEmployees(page: Int = 1) {
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }
            try {
                val response = api.getEmployees(page = page)
                if (response.isSuccessful && response.body() != null) {
                    val employeeList = response.body()!!.data
                    _state.update { it.copy(dataState = UiState.Success(employeeList)) }
                } else {
                    _state.update { it.copy(dataState = UiState.Error("Gagal mengambil data: ${response.code()}")) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(dataState = UiState.Error("Failed to connect to the server ${e.message}")) }
            }
        }
    }

    fun searchRoles(query: String) {
        searchRoleJob?.cancel()
        searchRoleJob = viewModelScope.launch {
            _isRoleLoading.value = true
            delay(300)
            try {
                val response = roleApi.getRole(search = query.ifBlank { null })
                if (response.isSuccessful && response.body() != null) {
                    _roles.value = response.body()!!.data
                } else {
                    _roles.value = emptyList()
                }
            } catch (e: Exception) {
                _roles.value = emptyList()
            } finally {
                _isRoleLoading.value = false
            }
        }
    }

    fun insertEmployee(fullName: String, email: String, password: String?, roleId: String, photoFile: File?){
        viewModelScope.launch {
            _state.update {
                it.copy(isActionLoading = true, actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap())
            }
            try {
                val textMediaType = "text/plain".toMediaTypeOrNull()
                val fields = HashMap<String, RequestBody>().apply {
                    put("FullName", fullName.toRequestBody(textMediaType))
                    put("Email", email.toRequestBody(textMediaType))
                    put("RoleId", roleId.toRequestBody(textMediaType))
                    if (!password.isNullOrBlank()) {
                        put("Password", password.toRequestBody(textMediaType))
                    }
                }

                val photoPart = photoFile?.let { file ->
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photo", file.name, requestBody)
                }

                val response = api.insertEmployee(fields, photoPart)
                if (response.isSuccessful) {
                    _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
                    fetchEmployees()
                } else {
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal menambah employee")
                    _state.update { it.copy(isActionLoading = false, actionErrorMessage = errorResult.generalErrorMessage, fieldErrors = errorResult.fieldErrors) }
                }
            } catch (e: Exception){
                _state.update { it.copy(isActionLoading = false, actionErrorMessage = "Gagal terhubung ke server: ${e.localizedMessage}") }
            }
        }
    }

    fun updateEmployee(id: String, fullName: String, email: String, password: String?, roleId: String, photoFile: File?) {
        viewModelScope.launch {
            _state.update {
                it.copy(isActionLoading = true, actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap())
            }
            try {
                val textMediaType = "text/plain".toMediaTypeOrNull()
                val fields = HashMap<String, RequestBody>().apply {
                    put("Id", id.toRequestBody(textMediaType))
                    put("FullName", fullName.toRequestBody(textMediaType))
                    put("Email", email.toRequestBody(textMediaType))
                    put("RoleId", roleId.toRequestBody(textMediaType))
                    if (!password.isNullOrBlank()) {
                        put("Password", password.toRequestBody(textMediaType))
                    }
                }

                val photoPart = photoFile?.let { file ->
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photo", file.name, requestBody)
                }

                val response = api.updateEmployee(id, fields, photoPart)
                if (response.isSuccessful) {
                    _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
                    fetchEmployees()
                } else {
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal mengubah employee")
                    _state.update { it.copy(isActionLoading = false, actionErrorMessage = errorResult.generalErrorMessage, fieldErrors = errorResult.fieldErrors) }
                }
            } catch (e: Exception){
                _state.update { it.copy(isActionLoading = false, actionErrorMessage = "Gagal terhubung ke server") }
            }
        }
    }

    fun deleteEmployee(id: String){
        viewModelScope.launch {
            _state.update {
                it.copy(isActionLoading = true, actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap())
            }
            try{
                val response = api.deleteEmployee(id)
                if(response.isSuccessful){
                    _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
                    fetchEmployees()
                } else {
                    val errorJson = response.errorBody()?.string()
                    if (errorJson != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
                            _state.update { it.copy(isActionLoading = false, actionErrorMessage = errorResponse.message ?: "Gagal menghapus data") }
                        } catch (e: Exception) {
                            _state.update { it.copy(isActionLoading = false, actionErrorMessage = "Terjadi kesalahan membaca error.") }
                        }
                    } else {
                        _state.update { it.copy(isActionLoading = false, actionErrorMessage = "Gagal menghapus data: Format tidak valid") }
                    }
                }
            } catch (e: Exception){
                _state.update { it.copy(isActionLoading = false, actionErrorMessage = "Gagal terhubung ke server: ${e.localizedMessage}") }
            }
        }
    }

    fun clearFullNameError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "FullName") }
    }

    fun clearEmailError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Email") }
    }

    fun clearPasswordError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Password") }
    }

    fun clearRoleIdError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "RoleId") }
    }

    fun clearGeneralError() {
        _state.update { it.copy(actionErrorMessage = null) }
    }

    fun resetActionState() {
        _state.update { it.copy(actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap()) }
    }
}