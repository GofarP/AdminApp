package com.gopro.AdminApp.viewmodel.itemcategories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.request.itemcategories.ItemCategoriesRequest
import com.gopro.AdminApp.model.dto.request.permissioncategories.PermissionCategoriesRequest
import com.gopro.AdminApp.model.dto.response.error.ErrorResponse
import com.gopro.AdminApp.model.entity.ItemCategories
import com.gopro.AdminApp.model.entity.PermissionCategories
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.network.itemcategories.ItemCategoriesApi
import com.gopro.AdminApp.presentation.state.ScreenState
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.utils.ApiErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ItemCategoriesViewModel: ViewModel() {

    private val api= RetrofitClient.createService(ItemCategoriesApi::class.java)

    private val _state= MutableStateFlow(ScreenState<List<ItemCategories>>())

    val state: StateFlow<ScreenState<List<ItemCategories>>> = _state.asStateFlow()

    init{
        fetchItemCategories()
    }

    fun fetchItemCategories(page:Int=1){
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }
            try{
                val response=api.getItemCategories(page=page)
                if(response.isSuccessful && response.body()!=null){
                    val itemCategoryList=response.body()!!.data
                    _state.update { it.copy(dataState = UiState.Success(itemCategoryList)) }
                }else{
                    _state.update { it.copy(dataState = UiState.Error("Gagal mengambil data: ${response.code()}")) }
                }
            }catch (e: Exception){
                _state.update { it.copy(dataState = UiState.Error("Failed to connect to the server")) }
                println(e.message)
            }
        }
    }

    fun insertItemCategories(name:String, description: String){
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
                val request= ItemCategoriesRequest(name=name, description=description)
                val response=api.insertItemCategories(request)
                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchItemCategories()
                }else{
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


    fun updateItemCategories(id: Int, name: String, description: String){
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
                val request= ItemCategoriesRequest(name=name, description=description)
                val response=api.updateItemCategories(id,request)

                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                    fetchItemCategories()
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

    fun deleteItemCategories(id:Int){
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
                val response=api.deleteItemCategories(id)
                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchItemCategories()
                } else{

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
            }
            catch (e: Exception){
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

    fun clearDescError(){
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Description") }
    }

    fun clearGeneralError() {
        _state.update { it.copy(actionErrorMessage = null) }
    }

    fun resetActionState() {
        _state.update { it.copy(actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap()) }
    }


}