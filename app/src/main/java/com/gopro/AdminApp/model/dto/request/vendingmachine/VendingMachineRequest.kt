package com.gopro.AdminApp.model.dto.request.vendingmachine

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class VendingMachineRequest(
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