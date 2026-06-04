package com.gopro.AdminApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.request.ItemRequest
import com.gopro.AdminApp.model.dto.response.ErrorResponse
import com.gopro.AdminApp.model.entity.Item
import com.gopro.AdminApp.model.entity.ItemCategories
import com.gopro.AdminApp.network.ItemApi
import com.gopro.AdminApp.network.ItemCategoriesApi
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

class ItemViewModel: ViewModel() {
    private  val api= RetrofitClient.createService(ItemApi::class.java)
    private val itemCategoriesApi= RetrofitClient.createService(ItemCategoriesApi::class.java)

    private  val _state= MutableStateFlow(ScreenState<List<Item>>())

    val state: StateFlow<ScreenState<List<Item>>> = _state.asStateFlow()

    private val _itemCategories = MutableStateFlow<List<ItemCategories>>(emptyList())

    val itemCategories: StateFlow<List<ItemCategories>> = _itemCategories.asStateFlow()

    private val _isCategoryLoading = MutableStateFlow(false)

    val isCategoryLoading: StateFlow<Boolean> = _isCategoryLoading.asStateFlow()

    private  var searchJob: Job?=null

    init{
        fetchItems()
        searchItemCategories("")
    }

    fun fetchItems(page:Int=1){
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }

            try{
                val response=api.getItem(page=page)
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

    fun searchItemCategories(query: String) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _isCategoryLoading.value = true
            delay(300)

            try {
                val response = itemCategoriesApi.getItemCategories(
                    page = 1,
                    limit = 20,
                    search = query.ifBlank { null }
                )

                if (response.isSuccessful && response.body() != null) {
                    _itemCategories.value = response.body()!!.data
                } else {
                    _itemCategories.value = emptyList()
                }
            } catch (e: Exception) {
                _itemCategories.value = emptyList()
                println("Error search category: ${e.message}")
            } finally {
                _isCategoryLoading.value = false
            }
        }
    }

    fun insertItem(name:String, itemCategoryId:Int, price: Double, quantity: Int){
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

                val request= ItemRequest(
                    name = name,
                    itemCategoryId = itemCategoryId,
                    price = price,
                    quantity = quantity
                )
                val response=api.insertItem(request)
                if (response.isSuccessful) {
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                    fetchItems()
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


    fun updateItem(id: Int, name: String, itemCategoriesId:Int, price: Double, quantity: Int){
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
                val request= ItemRequest(
                    id = id,
                    name = name,
                    itemCategoryId = itemCategoriesId,
                    price = price,
                    quantity = quantity
                )
                val response=api.updateItem(id,request)

                if (response.isSuccessful) {
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                    fetchItems()
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

            }catch (e: Exception){
                _state.update {
                    it.copy(
                        isActionLoading = false,
                        actionErrorMessage = "Gagal terhubung ke server"
                    )
                }
            }
        }

    }


    fun deleteItem(id: Int){
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
                val response=api.deleteItem(id)

                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchItems()
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

    fun clearItemCategoryIdError(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"ItemCategoryId") }
    }

    fun clearPriceError(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"Price") }
    }

    fun clearQuantityError(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"Quantity") }
    }

    fun clearGeneralError() {
        _state.update { it.copy(actionErrorMessage = null) }
    }

    fun resetActionState() {
        _state.update { it.copy(actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap()) }
    }


}