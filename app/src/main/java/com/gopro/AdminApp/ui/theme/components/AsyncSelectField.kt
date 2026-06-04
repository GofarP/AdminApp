package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AsyncSelectField(
    selectedOption: T?,
    options: List<T>,
    onSearchQueryChanged: (String) -> Unit,
    onOptionSelected: (T) -> Unit,
    displayMapper: (T) -> String,
    modifier: Modifier = Modifier,
    placeholder: String = "Pilih Data",
    isLoading: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String = ""
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = selectedOption?.let { displayMapper(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error else LocalContentColor.current
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isError) 1.dp else 0.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            enabled = true,
            isError = isError,
            supportingText = if (isError && errorText.isNotEmpty()) {
                { Text(text = errorText, color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF4F5F7),
                unfocusedContainerColor = Color(0xFFF4F5F7),
                disabledContainerColor = Color(0xFFF4F5F7),

                // Background menjadi merah pudar saat error
                errorContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),

                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,

                // Teks placeholder tetap berwarna abu-abu meski error
                errorPlaceholderColor = Color.Gray
            )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (enabled) {
                        showBottomSheet = true
                        searchQuery = ""
                        onSearchQueryChanged("")
                    }
                }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
                    .imePadding()
            ) {
                Text(text = placeholder, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { text ->
                        searchQuery = text
                        onSearchQueryChanged(text)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f)) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (options.isEmpty()) {
                        Text(
                            text = "Data tidak ditemukan",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(options) { item ->
                                ListItem(
                                    headlineContent = { Text(displayMapper(item)) },
                                    modifier = Modifier.clickable {
                                        onOptionSelected(item)
                                        showBottomSheet = false
                                    }
                                )
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}