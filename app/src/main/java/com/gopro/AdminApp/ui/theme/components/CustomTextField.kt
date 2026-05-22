package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    isError: Boolean = false,       // Parameter baru untuk mendeteksi status error
    errorText: String = ""          // Parameter baru untuk menampung pesan teks error
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isError) 1.dp else 0.dp,
                    color = if (isError) Color(0xFFEF4444) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            enabled = enabled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF3F4F6),
                unfocusedContainerColor = Color(0xFFF3F4F6),
                disabledContainerColor = Color(0xFFE5E7EB),
                errorContainerColor = Color(0xFFFEE2E2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            trailingIcon = {
                if (isPassword) {
                    Text(
                        text = if (passwordVisible) "HIDE" else "SHOW",
                        color = if (isError) Color(0xFFEF4444) else Color(0xFF0F62FE), // Ikon ikut memerah jika error
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { passwordVisible = !passwordVisible }
                    )
                }
            }
        )

        if (isError && errorText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                color = Color(0xFFEF4444),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}