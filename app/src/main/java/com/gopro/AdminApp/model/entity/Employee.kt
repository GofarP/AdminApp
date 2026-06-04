package com.gopro.AdminApp.model.entity

import com.google.gson.annotations.SerializedName

data class Employee(
    val id: String,
    val fullName: String,
    val email: String,
    val password: String? = null,
    val photo: String? = null,

    @SerializedName("photoUrl")
    val photoUrl: String? = null,

    @SerializedName("role")
    val role: Role? = null
)