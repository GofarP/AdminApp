package com.gopro.AdminApp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gopro.AdminApp.ui.theme.components.CustomButton

@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Konfirmasi Hapus", fontWeight = FontWeight.Bold)
        },
        text = {
            Text("Apakah Anda yakin ingin menghapus data '$itemName'? Tindakan ini tidak dapat dibatalkan.")
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),

        confirmButton = {
            Box(modifier = Modifier.width(120.dp)) {
                CustomButton(
                    text = "Hapus",
                    isLoading = false,
                    onClick = onConfirm
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color(0xFF64748B))
            }
        }
    )
}