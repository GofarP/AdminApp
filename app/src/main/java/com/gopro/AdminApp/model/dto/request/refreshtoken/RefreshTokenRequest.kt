package com.gopro.AdminApp.model.dto.request.refreshtoken

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("AccessToken") val accessToken: String,
    @SerializedName("RefreshToken") val refreshToken: String
)