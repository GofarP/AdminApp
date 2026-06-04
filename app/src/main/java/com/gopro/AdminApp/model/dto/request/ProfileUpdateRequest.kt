package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest (
    @SerializedName("Id")
    val id: String? = null,

    @SerializedName("FullName")
    val fullName: String?=null,

    @SerializedName("Email")
    val email:String?=null,
)