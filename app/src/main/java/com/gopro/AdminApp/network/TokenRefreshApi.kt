package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.dto.request.RefreshTokenRequest
import com.gopro.AdminApp.model.dto.response.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenRefreshApi {
    @POST("api/auth/refresh")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<LoginResponse>
}