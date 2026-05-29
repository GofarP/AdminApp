package com.gopro.AdminApp.network.vendingmachine

import com.gopro.AdminApp.model.dto.request.itemcategories.ItemCategoriesRequest
import com.gopro.AdminApp.model.dto.request.vendingmachine.VendingMachineRequest
import com.gopro.AdminApp.model.dto.response.vendingmachine.VendingMachineResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VendingMachineApi {
    @GET("api/vendingmachine")
    suspend fun getVendingMachine(
        @Query("page")page: Int=1,
        @Query("limit")limit: Int=1
    ): Response<VendingMachineResponse>

    @POST("api/vendingmachine")
    suspend fun  insertVendingMachine(
        @Body request: VendingMachineRequest
    ): Response<Unit>

    @PUT("api/vendingmachine/{id}")
    suspend fun updateVendingMachine(
        @Path("id") id: Int,
        @Body request: VendingMachineRequest
    ): Response<Unit>

    @DELETE("api/vendingmachine/{id}")
    suspend fun deleteVendingMachine(
        @Path("id") id: Int
    ): Response<Unit>

}