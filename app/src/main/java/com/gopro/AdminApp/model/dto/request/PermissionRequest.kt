package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName

data class PermissionRequest(
    @SerializedName("Id")
    val id: Int? = null,

    @SerializedName("Name")
    val name: String,

    @SerializedName("PermissionCategoryId")
    val permissionCategoryId: Int
)