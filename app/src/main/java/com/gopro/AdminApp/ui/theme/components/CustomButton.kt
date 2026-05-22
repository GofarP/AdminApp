package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonColorType {
    PRIMARY, SUCCESS, DANGER, SECONDARY
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    colorType: ButtonColorType = ButtonColorType.PRIMARY
){
    val containerColor = when (colorType) {
        ButtonColorType.PRIMARY -> Color(0xFF0F62FE)
        ButtonColorType.SUCCESS -> Color(0xFF10B981)
        ButtonColorType.DANGER -> Color(0xFFEF4444)
        ButtonColorType.SECONDARY -> Color(0xFF6B7280)
    }

    val disabledContainerColor = when (colorType) {
        ButtonColorType.PRIMARY -> Color(0xFF7FA8FF)
        ButtonColorType.SUCCESS -> Color(0xFF6EE7B7)
        ButtonColorType.DANGER -> Color(0xFFFCA5A5)
        ButtonColorType.SECONDARY -> Color(0xFFD1D5DB)
    }

    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = Color.White
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        if(isLoading){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Memuat...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }else{
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}