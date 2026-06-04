package com.gopro.AdminApp.model.dto.request

import com.google.gson.annotations.SerializedName

data class ItemCategoriesRequest(
    @SerializedName("Id")
    val id: Int? = null,

    @SerializedName("Name")
    val name: String,

    @SerializedName("Description")
    val description: String
)