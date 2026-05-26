package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gopro.AdminApp.model.presentation.MenuItem
import com.gopro.AdminApp.ui.theme.BrandLight
import com.gopro.AdminApp.ui.theme.CardBg
import com.gopro.AdminApp.ui.theme.Coral
import com.gopro.AdminApp.ui.theme.TextPri

@Composable
fun MenuButton(
    item: MenuItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val bgColor=if(isSelected) BrandLight else CardBg
    val textColor = if (isSelected) Color.White else TextPri
    val iconColor = if (isSelected) Color.White else item.accentColor
    val elevationValue=if(isSelected) 0.dp else 2.dp
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(elevation = elevationValue, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = iconColor,
                    modifier = Modifier.padding(4.dp).size(32.dp)
                )

                if (item.badge != null) {
                    Box(
                        modifier = Modifier
                            .offset(8.dp, (-4).dp)
                            .shadow(elevation = 2.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Coral)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(item.badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text       = item.label,
                fontSize   = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color      = textColor,
                maxLines   = 2,
                textAlign  = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}