package com.gopro.AdminApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.request.PermissionRequest
import com.gopro.AdminApp.model.dto.response.ErrorResponse
import com.gopro.AdminApp.model.entity.Permission
import com.gopro.AdminApp.model.entity.PermissionCategories
import com.gopro.AdminApp.network.PermissionApi
import com.gopro.AdminApp.network.PermissionCategoryApi
import com.gopro.AdminApp.network.RetrofitClient
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

class PermissionViewModel: ViewModel(){
    private val api= RetrofitClient.createService(PermissionApi::class.java)
    private val permissionCategoryApi= RetrofitClient.createService(PermissionCategoryApi::class.java)

    private  val _state= MutableStateFlow(ScreenState<List<Permission>>())

    val state: StateFlow<ScreenState<List<Permission>>> = _state.asStateFlow()

    private val _permissionCategories = MutableStateFlow<List<PermissionCategories>>(emptyList())
    val permissionCategories: StateFlow<List<PermissionCategories>> = _permissionCategories.asStateFlow()

    private val _isCategoryLoading = MutableStateFlow(false)
    val isCategoryLoading: StateFlow<Boolean> = _isCategoryLoading.asStateFlow()

    private var searchJob: Job?=null

    init{
        fetchPermissions()
        searchCategories("")
    }

    fun fetchPermissions(page:Int=1){
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }

            try{
                val response=api.getPermission(page=page)
                if(response.isSuccessful && response.body() != null){
                    val permissionList = response.body()!!.data

                    _state.update { it.copy(dataState = UiState.Success(permissionList)) }
                }else{
                    _state.update { it.copy(dataState = UiState.Error("Gagal mengambil data: ${response.code()}")) }
                }
            }catch (e: Exception){
                _state.update { it.copy(dataState = UiState.Error("Failed to connect to the server")) }
                println(e.message)
            }
        }
    }

    fun searchCategories(query: String) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _isCategoryLoading.value = true
            delay(300)

            try {
                val response = permissionCategoryApi.getPermissionCategories(
                    page = 1,
                    limit = 20,
                    search = query.ifBlank { null }
                )

                if (response.isSuccessful && response.body() != null) {
                    _permissionCategories.value = response.body()!!.data
                } else {
                    _permissionCategories.value = emptyList()
                }
            } catch (e: Exception) {
                _permissionCategories.value = emptyList()
                println("Error search category: ${e.message}")
            } finally {
                _isCategoryLoading.value = false
            }
        }
    }
    fun insertPermission(name: String, permissionCategoryId: Int){
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
                val request =
                    PermissionRequest(name = name, permissionCategoryId = permissionCategoryId)
                val response = api.insertPermission(request)

                if (response.isSuccessful) {
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                    fetchPermissions()
                } else {
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal menambah permission")
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


    fun updatePermission(id:Int,name: String, permissionCategoryId: Int){
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
                val request = PermissionRequest(
                    id = id,
                    name = name,
                    permissionCategoryId = permissionCategoryId
                )
                val response = api.updatePermission(id,request)

                if (response.isSuccessful) {
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                    fetchPermissions()
                } else {
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal menambah permission")
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

    fun deletePermission(id: Int){
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
                val response=api.deletePermission(id)

                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchPermissions()
                }
                else{

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

    fun clearNameError(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"Name") }
    }

    fun clearPermissionCategoryIdError(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"PermissionId") }
    }

    fun clearGeneralError() {
        _state.update { it.copy(actionErrorMessage = null) }
    }

    fun resetActionState() {
        _state.update { it.copy(actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap()) }
    }

}