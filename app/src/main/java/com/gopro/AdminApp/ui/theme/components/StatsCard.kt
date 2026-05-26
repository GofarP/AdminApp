package com.gopro.AdminApp.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gopro.AdminApp.model.presentation.StatItem
import com.gopro.AdminApp.ui.theme.CardBg
import com.gopro.AdminApp.ui.theme.TextHint
import com.gopro.AdminApp.ui.theme.TextPri
import com.gopro.AdminApp.ui.theme.TextSec

@Composable
fun StatsCard(stat: StatItem, modifier: Modifier= Modifier){
    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Column {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(stat.color.copy(.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(stat.icon, null, tint = stat.color, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.height(10.dp))
            Text(stat.label, fontSize = 11.sp, color = TextSec)
            Text(stat.value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPri)
            Text(stat.sub,   fontSize = 10.sp, color = TextHint)
        }
    }
}