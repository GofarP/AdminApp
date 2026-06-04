package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class PasswordUpdateRequest(
    @SerializedName("OldPassword")
    val oldPassword: String? = "",

    @SerializedName("NewPassword")
    val newPassword: String?="",

    @SerializedName("ConfirmPassword")
    val confirmPassword: String?

)
