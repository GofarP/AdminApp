package com.gopro.AdminApp.model.dto.request.permissioncategories

import com.google.gson.annotations.SerializedName

data class PermissionCategoriesRequest(
    @SerializedName("Name")
    val name: String,

    @SerializedName("Description")
    val description: String
)