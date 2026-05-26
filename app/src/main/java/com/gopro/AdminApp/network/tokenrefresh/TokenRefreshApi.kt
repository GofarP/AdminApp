package com.gopro.AdminApp.network.tokenrefresh

import com.gopro.AdminApp.model.dto.request.refreshtoken.RefreshTokenRequest
import com.gopro.AdminApp.model.dto.response.auth.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenRefreshApi {
    @POST("api/auth/refresh")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<LoginResponse>
}