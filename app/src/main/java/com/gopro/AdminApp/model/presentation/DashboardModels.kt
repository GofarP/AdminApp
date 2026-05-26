package com.gopro.AdminApp.model.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class  MenuGroup(val groupLabel:String, val items:List<MenuItem>)

data class MenuItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badge: String? = null,
    val targetActivity: Class<*>? = null
)

data class StatItem(
    val label: String,
    val value: String,
    val sub: String,
    val icon: ImageVector,
    val color: Color
)

