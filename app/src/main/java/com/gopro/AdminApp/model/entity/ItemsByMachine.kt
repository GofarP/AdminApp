package com.gopro.AdminApp.model.entity

import com.google.gson.annotations.SerializedName

data class ItemsByMachine(
    @SerializedName("id")
    val id: Int,

    @SerializedName("vendingMachineId")
    val vendingMachineId: Int,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("capacity")
    val capacity: Int,

    @SerializedName("price")
    val price: Double,

    @SerializedName("itemName")
    val itemName: String,

    @SerializedName("categoryName")
    val categoryName: String
)