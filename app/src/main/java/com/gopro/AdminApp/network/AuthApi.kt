package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.dto.request.LoginRequest
import com.gopro.AdminApp.model.dto.request.LogoutRequest
import com.gopro.AdminApp.model.dto.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi{
    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logoutUser(@Body request: LogoutRequest): Response<Unit>
}