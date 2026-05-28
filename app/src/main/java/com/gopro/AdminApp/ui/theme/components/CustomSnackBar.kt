package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.Warning

enum class SnackbarType{
    SUCCESS, ERROR, WARNING
}

class CustomSnackbarVisuals(
    override val message: String,
    val type: SnackbarType,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = true,
    override val duration: SnackbarDuration = SnackbarDuration.Short
) : SnackbarVisuals

@Composable
fun CustomSnackBar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
){
    val customVisuals = snackbarData.visuals as? CustomSnackbarVisuals
    if(customVisuals!=null){
        val backgroundColor = when (customVisuals.type) {
            SnackbarType.SUCCESS -> Color(0xFF4CAF50)
            SnackbarType.ERROR -> Error
            SnackbarType.WARNING -> Warning
        }

        val icon: ImageVector = when (customVisuals.type) {
            SnackbarType.SUCCESS -> Icons.Outlined.CheckCircle
            SnackbarType.ERROR -> Icons.Outlined.ErrorOutline
            SnackbarType.WARNING -> Icons.Outlined.WarningAmber
        }

        Row(
            modifier=modifier
                .padding(16.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                .fillMaxWidth()
                .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = customVisuals.message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (customVisuals.withDismissAction) {
                IconButton(
                    onClick = { snackbarData.dismiss() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }else{
                Snackbar(snackbarData = snackbarData)
            }
        }
    }
}