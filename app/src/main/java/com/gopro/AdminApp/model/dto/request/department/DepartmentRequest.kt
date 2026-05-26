package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName

data class DepartmentRequest(
    @SerializedName("Name")
    val name: String,

    @SerializedName("Description")
    val description: String
)