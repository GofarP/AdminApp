package com.gopro.AdminApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.request.RoleRequest
import com.gopro.AdminApp.model.dto.response.ErrorResponse
import com.gopro.AdminApp.model.entity.Permission
import com.gopro.AdminApp.model.entity.Role
import com.gopro.AdminApp.network.PermissionApi
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

class RoleViewModel: ViewModel() {
    private val api= RetrofitClient.createService(RoleApi::class.java)
    private val permissionApi= RetrofitClient.createService(PermissionApi::class.java)

    private val _state= MutableStateFlow(ScreenState<List<Role>>())
    val state: StateFlow<ScreenState<List<Role>>> = _state.asStateFlow()

    private val _groupedPermissions = MutableStateFlow<Map<String, List<Permission>>>(emptyMap())
    val groupedPermissions: StateFlow<Map<String, List<Permission>>> = _groupedPermissions.asStateFlow()

    private val _isPermissionLoading= MutableStateFlow(false)
    val isPermissionLoading: StateFlow<Boolean> = _isPermissionLoading.asStateFlow()

    private var searchJob: Job?=null

    init {
        fetchRoles()
        searchPermissions("")
    }

    fun fetchRoles(page:Int=1){
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }

            try {
                val response = api.getRole(page = page)
                if (response.isSuccessful && response.body() != null) {
                    val roleList = response.body()!!.data
                    _state.update { it.copy(dataState = UiState.Success(roleList)) }
                } else {
                    _state.update { it.copy(dataState = UiState.Error("Gagal mengambil data: ${response.code()}")) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(dataState = UiState.Error("Failed to connect to the server ${e.localizedMessage}"))
                }
            }
        }
    }


    fun searchPermissions(query: String){
        searchJob?.cancel()
        searchJob=viewModelScope.launch {
            _isPermissionLoading.value=true
            delay(300)
            try{
                val response=permissionApi.getPermission(
                    page=1,
                    limit = 200,
                    search = query.ifBlank { null }
                )

                if(response.isSuccessful && response.body() !== null){
                    val rawPermissions=response.body()!!.data
                    _groupedPermissions.value=rawPermissions.groupBy {
                        it.category?.name ?: "Tanpa Kategori"
                    }
                }else{
                    _groupedPermissions.value = emptyMap()
                }
            }catch (e: Exception){
                _groupedPermissions.value=emptyMap()
            }finally {
                _isPermissionLoading.value=false
            }
        }
    }

    fun insertRole(name:String, permissionIds:List<Int>){
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isActionLoading = true,
                    actionSuccess = false,
                    actionErrorMessage = null,
                    fieldErrors = emptyMap()
                )
            }

            try{
                val request = RoleRequest(name = name, permissionId = permissionIds)
                val response=api.insertRole(request)
                if(response.isSuccessful){
                    _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
                    fetchRoles()
                }else{
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal menambah role")
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            actionErrorMessage = errorResult.generalErrorMessage,
                            fieldErrors = errorResult.fieldErrors
                        )
                    }
                }

            }catch (e: Exception){
                _state.update {
                    it.copy(
                        isActionLoading = false,
                        actionErrorMessage = "Gagal terhubung ke server: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun updateRole(id: String, name: String, permissionIds:List<Int>){
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
                val request = RoleRequest(id = id, name = name, permissionId = permissionIds)
                val response = api.updateRole(id, request)
                if(response.isSuccessful){
                    _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
                    fetchRoles()
                }else{
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal mengubah role")
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            actionErrorMessage = errorResult.generalErrorMessage,
                            fieldErrors = errorResult.fieldErrors
                        )
                    }
                }
            }catch (e: Exception){
                _state.update {
                    it.copy(
                        isActionLoading = false,
                        actionErrorMessage = "Gagal terhubung ke server: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun deleteRole(id: String) {
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
                val response = api.deleteRole(id)

                if (response.isSuccessful) {
                    _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
                    fetchRoles()
                } else {
                    val errorJson = response.errorBody()?.string()
                    if (errorJson != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)

                            _state.update {
                                it.copy(
                                    isActionLoading = false,
                                    actionErrorMessage = errorResponse.message ?: "Gagal menghapus data"
                                )
                            }
                        } catch (e: Exception) {
                            _state.update {
                                it.copy(isActionLoading = false, actionErrorMessage = "Terjadi kesalahan membaca error.")
                            }
                        }
                    } else {
                        _state.update {
                            it.copy(isActionLoading = false, actionErrorMessage = "Gagal menghapus data: Format tidak valid")
                        }
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

    fun clearNameError(){
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Name") }
    }

    fun clearPermissionError(){
        _state.update { it.copy(fieldErrors = it.fieldErrors - "PermissionIds") }
    }


    fun clearGeneralError(){
        _state.update { it.copy(actionErrorMessage = null) }
    }

    fun resetActionState() {
        _state.update { it.copy(actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap()) }
    }


}