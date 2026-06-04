package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.dto.request.ItemRequest
import com.gopro.AdminApp.model.dto.response.ItemResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ItemApi {
    @GET("api/item")
    suspend fun getItem(
        @Query("search")search:String?="",
        @Query("page")page: Int=1,
        @Query("limit")limit: Int=1
    ): Response<ItemResponse>

    @POST("api/item")
    suspend fun  insertItem(
        @Body request: ItemRequest
    ): Response<Unit>

    @PUT("api/item/{id}")
    suspend fun updateItem(
        @Path("id") id: Int,
        @Body request: ItemRequest
    ): Response<Unit>

    @DELETE("api/item/{id}")
    suspend fun deleteItem(
        @Path("id") id: Int
    ): Response<Unit>
}