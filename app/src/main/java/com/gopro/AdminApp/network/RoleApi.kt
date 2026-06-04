package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.dto.request.RoleRequest
import com.gopro.AdminApp.model.dto.response.RoleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RoleApi{
    @GET("api/role")
    suspend fun  getRole(
        @Query("search")search:String?="",
        @Query("page")page: Int=1,
        @Query("limit")limit:Int=1,
    ): Response<RoleResponse>

    @POST("api/role")
    suspend fun  insertRole(
        @Body request: RoleRequest
    ): Response<Unit>

    @PUT("api/role/{id}")
    suspend fun updateRole(
        @Path("id") id: String,
        @Body request: RoleRequest
    ): Response<Unit>

    @DELETE("api/role/{id}")
    suspend fun deleteRole(
        @Path("id") id: String
    ): Response<Unit>

}