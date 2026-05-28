package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.ui.theme.CardBg
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.TextPri
import com.gopro.AdminApp.ui.theme.TextSec
import com.gopro.AdminApp.ui.theme.Warning

@Composable
fun BaseMasterCard(
    title:String,
    description: String,
    formTitle: String,
    isActionLoading: Boolean,
    actionSuccess: Boolean,
    onResetActionState: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveEdit: () -> Unit,
    editFormContent: @Composable ColumnScope.() -> Unit
){
    var showEditForm by remember { mutableStateOf(false) }

    LaunchedEffect(actionSuccess) {
        if (actionSuccess) {
            showEditForm = false
            onResetActionState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .padding(16.dp)
    ){
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPri)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 13.sp, color = TextSec, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ){
                OutlinedButton(
                    onClick = { showEditForm = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    border = BorderStroke(1.dp, Warning)
                ) {
                    Text("Edit", fontSize = 12.sp, color = Warning)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDeleteClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    border = BorderStroke(1.dp, Error)
                ) {
                    Text("Hapus", fontSize = 12.sp, color = Error)
                }
            }
        }
    }

    if (showEditForm) {
        FormBottomSheet(
            title = formTitle,
            buttonText = "Simpan",
            isLoading = isActionLoading,
            onDismiss = {
                showEditForm = false
                onResetActionState()
            },
            onSave = onSaveEdit
        ) {
            editFormContent()
        }
    }
}

