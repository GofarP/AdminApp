package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName

data class RestockRequest(
    @SerializedName("Quantity")
    val quantity: Int,

    @SerializedName("Capacity")
    val capacity: Int
)