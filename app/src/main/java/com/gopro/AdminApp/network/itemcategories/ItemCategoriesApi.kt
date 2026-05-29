package com.gopro.AdminApp.network.itemcategories

import com.gopro.AdminApp.model.dto.request.itemcategories.ItemCategoriesRequest
import com.gopro.AdminApp.model.dto.response.itemcategories.ItemCategoriesResponse
import com.gopro.AdminApp.model.entity.ItemCategories
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ItemCategoriesApi {
    @GET("api/itemcategory")
    suspend fun getItemCategories(
        @Query("page")page: Int=1,
        @Query("limit")limit: Int=1
    ): Response<ItemCategoriesResponse>

    @POST("api/itemcategory")
    suspend fun  insertItemCategories(
        @Body request: ItemCategoriesRequest
    ): Response<Unit>

    @PUT("api/itemcategory/{id}")
    suspend fun updateItemCategories(
        @Path("id") id: Int,
        @Body request: ItemCategoriesRequest
    ): Response<Unit>

    @DELETE("api/itemcategory/{id}")
    suspend fun deleteItemCategories(
        @Path("id") id: Int
    ): Response<Unit>


}