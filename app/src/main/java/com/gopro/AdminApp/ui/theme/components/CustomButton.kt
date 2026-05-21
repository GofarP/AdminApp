package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class ButtonColorType{
    PRIMARY,
    SUCCESS,
    DANGER,
    SECONDARY
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorType: ButtonColorType = ButtonColorType.PRIMARY // Nilai default jika tidak diatur
){
    val buttonColors = when (colorType) {
        ButtonColorType.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3), // Biru
            contentColor = Color.White
        )
        ButtonColorType.SUCCESS -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50), // Hijau
            contentColor = Color.White
        )
        ButtonColorType.DANGER -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE53935), // Merah
            contentColor = Color.White
        )
        ButtonColorType.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF757575), // Abu-abu
            contentColor = Color.White
        )
    }

    Button(
        onClick = onClick,
        colors = buttonColors,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = text)
    }
}
@Preview(showBackground = true)
@Composable
fun CustomButtonPreview() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        CustomButton(
            text = "Tombol Utama (Primary)",
            onClick = {},
            colorType = ButtonColorType.PRIMARY
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomButton(
            text = "Tombol Sukses (Success)",
            onClick = {},
            colorType = ButtonColorType.SUCCESS
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomButton(
            text = "Tombol Bahaya (Danger)",
            onClick = {},
            colorType = ButtonColorType.DANGER
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomButton(
            text = "Tombol Batalkan (Secondary)",
            onClick = {},
            colorType = ButtonColorType.SECONDARY
        )
    }
}