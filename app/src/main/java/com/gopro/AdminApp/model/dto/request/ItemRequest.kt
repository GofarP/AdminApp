package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName

data class ItemRequest(
    @SerializedName("Id")
    val id: Int?=null,
    @SerializedName("Name")
    val name: String?=null,
    @SerializedName("ItemCategoryId")
    val itemCategoryId: Int,

    @SerializedName("Price")
    val price: Double,
    @SerializedName("Quantity")
    val quantity: Int
)