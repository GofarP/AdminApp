package com.gopro.AdminApp.model.dto.request.itemcategories

import com.google.gson.annotations.SerializedName

data class ItemCategoriesRequest(
    @SerializedName("Name")
    val name: String,

    @SerializedName("Description")
    val description: String
)