package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private fun formatForDisplay(dateString: String): String {
    return if (dateString.contains("T")) dateString.substringBefore("T") else dateString
}

@Composable
fun CustomDatePickerField(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String = ""
) {
    val displayValue = formatForDisplay(value)

    Box(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder) },
            trailingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Pilih Tanggal") },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            supportingText = if (isError && errorText.isNotEmpty()) {
                { Text(text = errorText, color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF4F5F7),
                unfocusedContainerColor = Color(0xFFF4F5F7),
                disabledContainerColor = Color(0xFFF4F5F7),
                errorContainerColor = Color(0xFFF4F5F7),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (enabled) onClick()
                }
        )
    }
}