package com.gopro.AdminApp.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String?,

    @SerializedName("refreshToken")
    val refreshToken: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("fullName")
    val fullName: String?,

    @SerializedName("photoUrl")
    val photoUrl: String?,

    @SerializedName("roles")
    val roles: List<String>?,

    @SerializedName("permissions")
    val permissions: List<String>?,

    @SerializedName("expiresIn")
    val expiresIn: Int?
)