package com.gopro.AdminApp.model.entity

import com.google.gson.annotations.SerializedName

data class Role(
    val id: String,
    val name: String,

    @SerializedName("permissionIds")
    val permissionIds: List<Int>? = emptyList(),
    @SerializedName("permissions")
    val permissions: List<String>? = emptyList()
)