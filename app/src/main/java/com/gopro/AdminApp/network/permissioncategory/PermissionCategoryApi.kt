package com.gopro.AdminApp.network.permissioncategory

import com.gopro.AdminApp.model.dto.request.DepartmentRequest
import com.gopro.AdminApp.model.dto.request.permissioncategories.PermissionCategoriesRequest
import com.gopro.AdminApp.model.dto.response.PermissionCategories.PermissionCategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PermissionCategoryApi {

    @GET("api/permissioncategory")
    suspend fun getPermissionCategories(
        @Query("page")page:Int=1,
        @Query("limit")limit:Int=1
    ): Response<PermissionCategoryResponse>

    @POST("api/permissioncategory")
    suspend fun insertPermissionCategories(
        @Body request: PermissionCategoriesRequest
    ): Response<Unit>

    @PUT("api/permissioncategory/{id}")
    suspend fun updatePermissionCategories(
        @Path("id") id: Int,
        @Body request: PermissionCategoriesRequest
    ): Response<Unit>

    @DELETE("api/permissioncategory/{id}")
    suspend fun deletePermissionCategories(
        @Path("id") id: Int
    ): Response<Unit>
}