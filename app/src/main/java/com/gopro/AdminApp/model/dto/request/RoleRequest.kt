package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName

data class RoleRequest(
    @SerializedName("Id")
    val id: String?=null,

    @SerializedName("Name")
    val name: String,

    @SerializedName("PermissionIds")
    val permissionId:List<Int>

)