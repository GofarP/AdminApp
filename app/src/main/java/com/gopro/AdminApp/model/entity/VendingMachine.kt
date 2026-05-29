package com.gopro.AdminApp.model.entity

data class VendingMachine(
    val id:Int,
    val machineCode: String,
    val name: String,
    val location: String,
    val isActive: Boolean,
    val lastRestock: String
)