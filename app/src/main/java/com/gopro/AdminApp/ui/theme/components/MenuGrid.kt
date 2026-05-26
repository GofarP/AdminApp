package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gopro.AdminApp.model.presentation.MenuItem

@Composable
fun MenuGrid(
    items: List<MenuItem>,
    selectedMenu: String?,
    onSelect: (MenuItem) -> Unit
){
    val rows = items.chunked(3)
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    MenuButton(
                        item       = item,
                        isSelected = selectedMenu == item.label,
                        onClick    = { onSelect(item) },
                        modifier   = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
