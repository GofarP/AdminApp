package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName

data class  AssignItemToMachine(
    @SerializedName("Id")
    val id: Int?=null,

    @SerializedName("VendingMachineId")
    val vendingMachineId:Int?=null,

    @SerializedName("ItemId")
    val itemId:Int?=null,

    @SerializedName("Quantity")
    val quantity:Int?=null,

    @SerializedName("Capacity")
    val capacity:Int?=null,

    @SerializedName("Price")
    val price: Double?=null
)