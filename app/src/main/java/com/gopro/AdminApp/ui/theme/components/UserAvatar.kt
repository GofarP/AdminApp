package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.gopro.AdminApp.BuildConfig
import com.gopro.AdminApp.ui.theme.Accent
import com.gopro.AdminApp.ui.theme.Brand

@Composable
fun UserAvatar(
    photoUrl: String?,
    fullName: String,
    modifier: Modifier = Modifier
) {
    val initialLetter = if (fullName.isNotBlank()) fullName.take(1).uppercase() else "?"

    val fullPhotoUrl = if (photoUrl.isNullOrBlank()) {
        null
    } else {
        val baseUrl = BuildConfig.BASE_URL.removeSuffix("/")
        val cleanPath = photoUrl.removePrefix("/")
        "$baseUrl/uploads/users/$cleanPath"
    }

    SubcomposeAsyncImage(
        model = fullPhotoUrl,
        contentDescription = "Profile Photo",
        modifier = modifier
            .size(38.dp)
            .shadow(elevation = 2.dp, shape = CircleShape)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    ) {
        val state = painter.state

        if (state is AsyncImagePainter.State.Success) {
            SubcomposeAsyncImageContent()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Brand, Accent))), // Pastikan Brand & Accent sudah di-import
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialLetter,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
