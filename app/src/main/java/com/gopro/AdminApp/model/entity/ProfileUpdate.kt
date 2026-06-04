package com.gopro.AdminApp.model.entity

data class ProfileUpdate(
    val id: String,
    val fullName: String,
    val email: String,
    val photo: String? = null,
)
