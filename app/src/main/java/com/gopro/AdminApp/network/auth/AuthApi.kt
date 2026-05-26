package com.gopro.AdminApp.network.auth

import com.gopro.AdminApp.model.dto.request.auth.LoginRequest
import com.gopro.AdminApp.model.dto.request.auth.LogoutRequest
import com.gopro.AdminApp.model.dto.response.auth.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi{
    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logoutUser(@Body request: LogoutRequest): Response<Unit>
}