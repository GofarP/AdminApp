package com.gopro.AdminApp.ui.theme.components
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.gopro.AdminApp.ui.theme.Brand
import com.gopro.AdminApp.ui.theme.TextSec

@Composable
fun TopBar(
    userName: String = "Guest",
    photoUrl: String? = null
){
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ){
        Column {
            Text("VendingIoT", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Brand)
            Text("Dashboard Control Panel", fontSize = 12.sp, color = TextSec)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            UserAvatar(
                photoUrl = photoUrl,
                fullName = userName
            )
        }
    }
}