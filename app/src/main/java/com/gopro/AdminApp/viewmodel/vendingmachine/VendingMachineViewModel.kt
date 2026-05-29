package com.gopro.AdminApp.viewmodel.vendingmachine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.request.DepartmentRequest
import com.gopro.AdminApp.model.dto.request.vendingmachine.VendingMachineRequest
import com.gopro.AdminApp.model.dto.response.error.ErrorResponse
import com.gopro.AdminApp.model.entity.Department
import com.gopro.AdminApp.model.entity.ItemCategories
import com.gopro.AdminApp.model.entity.VendingMachine
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.network.vendingmachine.VendingMachineApi
import com.gopro.AdminApp.presentation.state.ScreenState
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.utils.ApiErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendingMachineViewModel: ViewModel() {
    private val api= RetrofitClient.createService(VendingMachineApi::class.java)

    private val _state= MutableStateFlow(ScreenState<List<VendingMachine>>())

    val state: StateFlow<ScreenState<List<VendingMachine>>> = _state.asStateFlow()


    init{
        fetchVendingMachine()
    }

    fun fetchVendingMachine(page:Int=1){
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }
            try{
                val response=api.getVendingMachine(page=page)
                if(response.isSuccessful && response.body()!=null){
                    val vendingMachineList=response.body()!!.data
                    _state.update { it.copy(dataState = UiState.Success(vendingMachineList)) }
                }else{
                    _state.update { it.copy(dataState = UiState.Error("Gagal mengambil data: ${response.code()}")) }
                }
            }catch (e: Exception){
                _state.update { it.copy(dataState = UiState.Error("Failed to connect to the server")) }
                println(e.message)
            }
        }
    }

    fun insertVendingMachine(
        machineCode: String,
        name: String,
        location: String,
        isActive:Boolean,
        lastRestock: String
    ){
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
                val request= VendingMachineRequest(machineCode = machineCode, name=name, location = location, isActive = isActive, lastRestock = lastRestock)
                val response=api.insertVendingMachine(request)
                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchVendingMachine()
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

    fun updateVendingMachine(
        id: Int,
        machineCode: String,
        name: String,
        location: String,
        isActive:Boolean,
        lastRestock: String
    ) {
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
                val request= VendingMachineRequest(machineCode = machineCode, name=name, location = location, isActive = isActive, lastRestock = lastRestock)
                val response = api.updateVendingMachine(id, request)

                if (response.isSuccessful) {
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                    fetchVendingMachine()
                } else {
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal menambah data")
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
    fun deleteVendingMachine(id: Int){
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
                val response=api.deleteVendingMachine(id)

                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchVendingMachine()
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

    fun clearMachineCodeError(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"MachineCode") }
    }


    fun clearName(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"Name") }
    }


    fun clearLocation(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"Location") }
    }

    fun clearIsActive(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"IsActive") }
    }

    fun clearLastRestock(){
        _state.update { it.copy(fieldErrors = it.fieldErrors-"LastRestock") }
    }

    fun clearGeneralError() {
        _state.update { it.copy(actionErrorMessage = null) }
    }

    fun resetActionState() {
        _state.update { it.copy(actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap()) }
    }









}