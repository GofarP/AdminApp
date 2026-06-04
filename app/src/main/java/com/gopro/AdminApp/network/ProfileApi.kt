package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.dto.request.PasswordUpdateRequest
import com.gopro.AdminApp.model.dto.response.PasswordUpdateResponse
import com.gopro.AdminApp.model.dto.response.ProfileResponse
import com.gopro.AdminApp.model.dto.response.ProfileUpdateResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part

interface ProfileApi{
    @GET("api/auth/me")
    suspend fun getProfile(): Response<ProfileResponse>

    @Multipart
    @PUT("api/profile/updateprofile")
    suspend fun  updateProfile(
        @Part fullName: MultipartBody.Part,
        @Part email: MultipartBody.Part,
        @Part photo: MultipartBody.Part?
    ): Response<ProfileUpdateResponse>

    @PUT("api/profile/changepassword")
    suspend fun updatePassword(
        @Body request: PasswordUpdateRequest
    ): Response<PasswordUpdateResponse>

}