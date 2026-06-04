package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.dto.request.AssignItemToMachine
import com.gopro.AdminApp.model.dto.request.RestockRequest
import com.gopro.AdminApp.model.dto.response.ItemsByMachineResponse
import com.gopro.AdminApp.model.dto.response.VendingWithStockResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VendingItemApi {
    @GET("api/vendingitem/vendingwithstock")
    suspend fun getVendingWithStock(
        @Query("search")search:String?="",
        @Query("page")page: Int=1,
        @Query("limit")limit: Int=1
    ): Response<VendingWithStockResponse>

    @GET("api/vendingitem/getitembymachine/{id}")
    suspend fun getItemsByMachine(
        @Path("id")id: Int,
        @Query("search")search:String?="",
        @Query("page")page: Int=1,
        @Query("limit")limit: Int=1
    ): Response<ItemsByMachineResponse>

    @POST("api/vendingitem/assignitemtomachine")
    suspend fun assignItemToMachine(
        @Body request: AssignItemToMachine
    ): Response<Unit>

    @DELETE("api/vendingitem/{id}")
    suspend fun removeItemFromMachine(
        @Path("id") id: String
    ): Response<Unit>

    @PUT("api/vendingitem/{id}/restock")
    suspend fun restock(
        @Path("id") id: Int,
        @Body request: RestockRequest
    ): Response<Unit>







}