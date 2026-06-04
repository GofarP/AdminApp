package com.gopro.AdminApp.ui.theme.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gopro.AdminApp.utils.FileUtil
import java.io.File

@Composable
fun ImagePickerField(
    selectedFile: File?,
    onFileSelected: (File?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Pilih Foto Profil (Opsional)",
    enabled: Boolean = true
){
    val context= LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
        if (uri != null) {
            val file = FileUtil.uriToFile(context, uri)
            onFileSelected(file)
        } else {
            onFileSelected(null)
        }
    }

    Column(modifier=modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(enabled = enabled) {
                    launcher.launch("image/*")
                }
                .padding(horizontal = 16.dp, vertical = 14.dp)

        ) {
            Icon(
                imageVector = Icons.Default.ImageSearch,
                contentDescription = "Pilih Gambar",
                tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = selectedFile?.name ?: placeholder,
                color = if (selectedFile != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }

        if (selectedFile != null && enabled) {
            TextButton(
                onClick = {
                    selectedUri = null
                    onFileSelected(null)
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Hapus Gambar", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }
    }


}