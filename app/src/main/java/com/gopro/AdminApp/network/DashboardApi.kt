package com.gopro.AdminApp.network

import com.gopro.AdminApp.model.dto.response.StatsResponse
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApi {
    @GET("api/home/stats")
    suspend fun getStats(): Response<StatsResponse>

}