package com.gopro.AdminApp.model.entity

import com.google.gson.annotations.SerializedName

data class Profile(
    val id: String,
    val fullName: String,
    val email: String,
    val photo: String? = null,
)