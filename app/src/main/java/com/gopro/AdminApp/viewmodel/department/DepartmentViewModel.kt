package com.gopro.AdminApp.viewmodel.department

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.request.DepartmentRequest
import com.gopro.AdminApp.model.dto.response.error.ErrorResponse
import com.gopro.AdminApp.model.entity.Department
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.network.department.DepartmentApi
import com.gopro.AdminApp.presentation.state.ScreenState
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.utils.ApiErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DepartmentViewModel: ViewModel(){
    private val api= RetrofitClient.createService(DepartmentApi::class.java)

    private  val _state= MutableStateFlow(ScreenState<List<Department>>())

    val state: StateFlow<ScreenState<List<Department>>> = _state.asStateFlow()

    init{
        fetchDepartments()
    }

    fun fetchDepartments(page:Int=1){
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }

            try{
                val response=api.getDepartments(page=page)

                if(response.isSuccessful && response.body() !=null){
                    val departmentList = response.body()!!.data

                    _state.update { it.copy(dataState = UiState.Success(departmentList)) }
                }else{
                    _state.update { it.copy(dataState = UiState.Error("Gagal mengambil data: ${response.code()}")) }
                }
            }catch (e: Exception){
                _state.update { it.copy(dataState = UiState.Error("Failed to connect to the server")) }
                println(e.message)
            }
        }
    }

    fun insertDepartment(name: String, description: String){
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
                val request= DepartmentRequest(name = name, description = description)
                val response=api.insertDepartment(request)
                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchDepartments()
                }

                else{
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal mengubah data")
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

    fun updateDepartment(id: Int, name: String, description: String) {
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
                val request = DepartmentRequest(name = name, description = description)
                val response = api.updateDepartment(id, request)

                if (response.isSuccessful) {
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }
                    fetchDepartments()
                } else {
                    val errorJson = response.errorBody()?.string()
                    if (errorJson != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)

                            val nameErr = errorResponse.errors?.get("Name")?.firstOrNull()
                            val descErr = errorResponse.errors?.get("Description")?.firstOrNull()

                            val newFieldErrors = mutableMapOf<String, String>()
                            if (!nameErr.isNullOrEmpty()) newFieldErrors["Name"] = nameErr
                            if (!descErr.isNullOrEmpty()) newFieldErrors["Description"] = descErr

                            var generalErr: String? = null
                            if (newFieldErrors.isEmpty()) {
                                generalErr = errorResponse.message ?: "Gagal mengubah data"
                            }

                            _state.update {
                                it.copy(
                                    isActionLoading = false,
                                    fieldErrors = newFieldErrors,
                                    actionErrorMessage = generalErr
                                )
                            }
                        } catch (e: Exception) {
                            _state.update {
                                it.copy(isActionLoading = false, actionErrorMessage = "Terjadi kesalahan membaca error.")
                            }
                        }
                    } else {
                        _state.update {
                            it.copy(isActionLoading = false, actionErrorMessage = "Gagal mengubah data: Format tidak valid")
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

    fun deleteDepartment(id: Int){
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
                val response=api.deleteDepartment(id)

                if(response.isSuccessful){
                    _state.update {
                        it.copy(isActionLoading = false, actionSuccess = true)
                    }

                    fetchDepartments()
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