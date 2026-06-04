package com.gopro.AdminApp.model.entity

import com.google.gson.annotations.SerializedName

data class Permission(
    val id:Int,
    val name: String,
    val permissionCategoryId: Int,
    @SerializedName("permissionCategory")
    val category: PermissionCategories? = null
)