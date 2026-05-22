package com.gopro.AdminApp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.ErrorResponse
import com.gopro.AdminApp.model.LoginRequest
import com.gopro.AdminApp.network.RetrofitClient
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel(){
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    var emailError by mutableStateOf("")
        private set

    var passwordError by mutableStateOf("")
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    fun login(request: LoginRequest){
        viewModelScope.launch {
            isLoading=true
            errorMessage=null
            emailError = ""
            passwordError = ""
            loginSuccess = false

            try{
                val response = RetrofitClient.instance.loginUser(request)
                if(response.isSuccessful && response.body()!=null){
                    loginSuccess=true
                }else{
                    val errorJson = response.errorBody()?.string()
                    if (errorJson != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)

                            // Mengambil pesan error validasi spesifik kolom dari ASP.NET Core Anda
                            emailError = errorResponse.errors?.get("Email")?.firstOrNull() ?: ""
                            passwordError = errorResponse.errors?.get("Password")?.firstOrNull() ?: ""

                            if (emailError.isEmpty() && passwordError.isEmpty()) {
                                errorMessage = errorResponse.message ?: "Login gagal"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Terjadi kesalahan saat membaca pesan error dari server."
                        }
                    } else {
                        errorMessage = "Login gagal: Format tidak valid"
                    }

                }
            }catch (e: Exception){
                errorMessage="Gagal terhubung ke server:${e.localizedMessage}"
            }finally {
                isLoading=false
            }
        }
    }
    fun clearEmailError() { emailError = "" }
    fun clearPasswordError() { passwordError = "" }
    fun clearGeneralError() { errorMessage = null }
}