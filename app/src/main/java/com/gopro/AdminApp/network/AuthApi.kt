package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.LoginRequest
import com.gopro.AdminApp.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface AuthApi{
    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
}