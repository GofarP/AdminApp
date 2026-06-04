package com.gopro.AdminApp.model.entity

import com.google.gson.annotations.SerializedName

data class Item(
    val id: Int,
    val name: String,
    val itemCategoryId: Int,
    val price: Double,
    val quantity: Int,
    val itemCategoryName: String?="",
)
