package com.gopro.AdminApp.viewmodel.permissioncategories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.request.permissioncategories.PermissionCategoriesRequest
import com.gopro.AdminApp.model.dto.response.error.ErrorResponse
import com.gopro.AdminApp.model.entity.PermissionCategories
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.network.permissioncategory.PermissionCategoryApi
import com.gopro.AdminApp.presentation.state.ScreenState
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.utils.ApiErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PermissionCategoriesViewModel: ViewModel() {
    private val api= RetrofitClient.createService(PermissionCategoryApi::class.java)
    private  val _state= MutableStateFlow(ScreenState<List<PermissionCategories>>())

    val state: StateFlow<ScreenState<List<PermissionCategories>>> = _state.asStateFlow()

    init{
        fetchPermissionCategories()
    }

    fun fetchPermissionCategories(page:Int=1){
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }

            try{
                val response=api.getPermissionCategories(page=page)
                if(response.isSuccessful && response.body() != null){
                    val permissionCategoryList = response.body()!!.data

                    _state.update { it.copy(dataState = UiState.Success(permissionCategoryList)) }
                }else{
                    _state.update { it.copy(dataState = UiState.Error("Gagal mengambil data: ${response.code()}")) }
                }
            }catch (e: Exception){
                _state.update { it.copy(dataState = UiState.Error("Failed to connect to the server")) }
                println(e.message)
            }
        }
    }

    fun insertPermissionCategories(name:String, description: String){
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

                val request= PermissionCategoriesRequest(name=name, description=description)
                val response=api.insertPermissionCategories(request)
                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchPermissionCategories()
                }

                else{
                    val errorResult= ApiErrorHandler.parseError(response, "gagal menambah permission categories")
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

    fun updatePermissionCategory(id:Int, name: String, description: String){
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
                val request= PermissionCategoriesRequest(name=name, description=description)
                val response=api.updatePermissionCategories(id,request)

                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                    fetchPermissionCategories()
                }
                else{
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal menambah data")
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            actionErrorMessage = errorResult.generalErrorMessage,
                            fieldErrors = errorResult.fieldErrors
                        )
                    }
                }
            }catch (e: Exception) {
                _state.update {
                    it.copy(
                        isActionLoading = false,
                        actionErrorMessage = "Gagal terhubung ke server: ${e.localizedMessage}"
                    )
                }
            }
        }
    }


    fun deletePermissionCategories(id: Int){
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
                val response=api.deletePermissionCategories(id)

                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchPermissionCategories()
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





}