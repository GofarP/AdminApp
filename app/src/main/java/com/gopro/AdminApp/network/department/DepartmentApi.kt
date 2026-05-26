package com.gopro.AdminApp.network.department

import com.gopro.AdminApp.model.dto.request.DepartmentRequest
import com.gopro.AdminApp.model.dto.response.department.DepartmentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DepartmentApi {

    @GET("api/department")
    suspend fun getDepartments(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<DepartmentResponse>

    @POST("api/department")
    suspend fun insertDepartment(
        @Body request: DepartmentRequest
    ): Response<Unit>

    @PUT("api/department/{id}")
    suspend fun updateDepartment(
        @Path("id") id: Int,
        @Body request: DepartmentRequest
    ): Response<Unit>

    @DELETE("api/department/{id}")
    suspend fun deleteDepartment(
        @Path("id") id: Int
    ): Response<Unit>
}