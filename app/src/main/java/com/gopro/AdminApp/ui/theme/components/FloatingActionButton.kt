package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gopro.AdminApp.ui.theme.Brand

@Composable
fun FloatingButton(
    onClick:()-> Unit,
    icon: ImageVector=Icons.Outlined.Add,
    contentDescription: String = "Tambah Data"
){
    FloatingActionButton(
        onClick = onClick,
        containerColor = Brand,
        contentColor = Color.White,
        shape = RoundedCornerShape(100.dp)
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}