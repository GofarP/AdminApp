package com.gopro.AdminApp.model.dto.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("fullName")
    val fullName: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("photoUrl")
    val photoUrl: String? = null,

    @SerializedName("roles")
    val roles: List<String>? = emptyList(),

    @SerializedName("permissions")
    val permissions: List<String>? = emptyList()
)