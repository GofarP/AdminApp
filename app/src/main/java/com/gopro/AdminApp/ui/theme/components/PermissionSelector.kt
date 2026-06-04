package com.gopro.AdminApp.ui.theme.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gopro.AdminApp.model.entity.Permission

@Composable
fun PermissionSelector(
    groupedPermissions: Map<String, List<Permission>>,
    selectedPermissionIds: List<Int>,
    onPermissionToggled: (permissionId: Int, isChecked: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val filteredPermissions = remember(searchQuery, groupedPermissions) {
        if (searchQuery.isBlank()) {
            groupedPermissions
        } else {
            groupedPermissions.mapValues { (_, permissions) ->
                permissions.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }.filterValues { it.isNotEmpty() }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {

        // --- 1. KOTAK PENCARIAN (DISESUAIKAN DENGAN DESAIN INPUT) ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                expanded = true
            },
            label = {
                Text(
                    if (selectedPermissionIds.isEmpty()) "Pilih Permission"
                    else "${selectedPermissionIds.size} Permission Terpilih",
                    fontWeight = FontWeight.SemiBold
                )
            },
            placeholder = { Text("Ketik untuk mencari...", color = Color.Gray, fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) expanded = true
                },
            singleLine = true, // Memaksa input hanya 1 baris
            shape = RoundedCornerShape(10.dp), // Membuat sudut melengkung sama seperti CustomTextField
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = Color.Gray
            ),
            trailingIcon = {
                IconButton(onClick = {
                    expanded = !expanded
                    if (!expanded) focusManager.clearFocus()
                }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Dropdown"
                    )
                }
            }
        )

        // --- 2. KONTEN DROPDOWN ---
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp) // Jarak sedikit dari form input
                    .heightIn(max = 300.dp), // Batas tinggi ideal dropdown
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Shadow sedikit lebih tebal agar terlihat melayang (pop-up)
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (groupedPermissions.isEmpty()) {
                        Text(
                            text = "Data permission belum tersedia.",
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (filteredPermissions.isEmpty()) {
                        Text(
                            text = "Permission tidak ditemukan.",
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        filteredPermissions.forEach { (categoryName, permissions) ->
                            Text(
                                text = categoryName.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )

                            permissions.forEach { permission ->
                                val isSelected = selectedPermissionIds.contains(permission.id)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPermissionToggled(permission.id, !isSelected) }
                                        .padding(vertical = 4.dp)
                                ) {
                                    // Checkbox diperkecil visual footprint-nya agar lebih rapi
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { isChecked ->
                                            onPermissionToggled(permission.id, isChecked)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = permission.name, fontSize = 14.sp)
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = Color.LightGray.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}