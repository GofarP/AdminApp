package com.gopro.AdminApp.model.entity

data class VendingWithStock(
    val id: Int,
    val name: String,
    val machineCode: String,
    val location: String,
    val totalItemTypes:Int,
    val totalStock:Int,
    val totalCategories:Int,
)