package com.gopro.AdminApp.model.dto.response

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message")
    val message: String?,

    @SerializedName("errors")
    val errors: Map<String, List<String>>?
)