package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.dto.request.PermissionRequest
import com.gopro.AdminApp.model.dto.response.PermissionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PermissionApi {
    @GET("api/permission")
    suspend fun getPermission(
        @Query("search")search:String?="",
        @Query("page")page: Int=1,
        @Query("limit")limit: Int=1
    ): Response<PermissionResponse>

    @POST("api/permission")
    suspend fun  insertPermission(
        @Body request: PermissionRequest
    ): Response<Unit>

    @PUT("api/permission/{id}")
    suspend fun updatePermission(
        @Path("id") id: Int,
        @Body request: PermissionRequest
    ): Response<Unit>

    @DELETE("api/permission/{id}")
    suspend fun deletePermission(
        @Path("id") id: Int
    ): Response<Unit>
}