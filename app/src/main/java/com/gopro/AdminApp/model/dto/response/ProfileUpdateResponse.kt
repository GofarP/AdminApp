package com.gopro.AdminApp.model.dto.response

import com.google.gson.annotations.SerializedName

data class ProfileUpdateResponse(
    @SerializedName("message")
    val message: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("fullName")
    val fullName: String?,

    @SerializedName("photoUrl")
    val photoUrl: String?,

    @SerializedName("roles")
    val roles: List<String>?,

    @SerializedName("permissions")
    val permissions: List<String>?
)
