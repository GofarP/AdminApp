package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.dto.response.EmployeeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface EmployeeApi {

    @GET("api/user")
    suspend fun getEmployees(
        @Query("search") search: String? = "",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
    ): Response<EmployeeResponse>

    @Multipart
    @POST("api/user")
    suspend fun insertEmployee(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photo: MultipartBody.Part?
    ): Response<Unit>

    @Multipart
    @PUT("api/user/{id}")
    suspend fun updateEmployee(
        @Path("id") id: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photo: MultipartBody.Part?
    ): Response<Unit>

    @DELETE("api/user/{id}")
    suspend fun deleteEmployee(
        @Path("id") id: String
    ): Response<Unit>
}