package com.gopro.AdminApp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp
import com.gopro.AdminApp.ui.theme.TextSec
import com.gopro.AdminApp.ui.theme.components.CustomTextField
import kotlinx.coroutines.delay
@Composable
fun<T> SearchableDataList(
    items:List<T>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchPlaceholder: String = "Cari data...",
    emptyMessage: String = "Data tidak ditemukan.",
    skeletonRowCount: Int = 5,
    debounceTimeout: Long = 500L,
    skeletonItem: @Composable () -> Unit,
    listItem: @Composable (item: T) -> Unit
){
    var internalQuery by remember { mutableStateOf(searchQuery) }

    LaunchedEffect(searchQuery) {
        if(internalQuery!=searchQuery){
            internalQuery=searchQuery
        }
    }

    LaunchedEffect(internalQuery) {
        if(internalQuery!=searchQuery){
            delay(debounceTimeout)
        }
    }

    LaunchedEffect(internalQuery) {
        if(internalQuery!=searchQuery){
            delay(debounceTimeout)
            onSearchQueryChange(internalQuery)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CustomTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = searchPlaceholder,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            containerColor = White,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search Icon",
                    tint = Gray
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when{
                isLoading->{
                    items(skeletonRowCount) { skeletonItem() }
                }
                items.isEmpty() -> {
                    item {
                        Text(
                            text = emptyMessage,
                            color = TextSec,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                else -> {
                    items(items) { itemData ->
                        listItem(itemData)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
