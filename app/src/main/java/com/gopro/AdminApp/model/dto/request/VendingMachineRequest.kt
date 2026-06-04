package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName

data class VendingMachineRequest(
    @SerializedName("Id")
    val id: Int? = null,

    @SerializedName("MachineCode")
    val machineCode: String,

    @SerializedName("Name")
    val name: String,

    @SerializedName("Location")
    val location: String,

    @SerializedName("IsActive")
    val isActive: Boolean,

    @SerializedName("LastRestock")
    val lastRestock: String
)