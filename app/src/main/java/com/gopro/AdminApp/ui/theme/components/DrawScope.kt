package com.gopro.AdminApp.ui.theme.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.drawOrb(center: Offset, radius: Float, color: Color) {
    drawCircle(
        Brush.radialGradient(listOf(color, Color.Transparent), center, radius),
        radius, center
    )
}
