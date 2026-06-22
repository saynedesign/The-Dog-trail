package com.saynedesign.habitloop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saynedesign.habitloop.ui.theme.BrandBackground
import com.saynedesign.habitloop.ui.theme.BrandBlue
import com.saynedesign.habitloop.ui.theme.BrandLightBlue
import com.saynedesign.habitloop.ui.theme.BrandSurface
import com.saynedesign.habitloop.ui.theme.TextPrimary
import com.saynedesign.habitloop.ui.theme.TextSecondary

@Composable
fun DaySelector(
    selectedDays: Set<Int>, // 0 = Sunday, 1 = Monday, etc.
    onDayToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = selectedDays.contains(index)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) BrandBlue else BrandBackground)
                    .clickable { onDayToggle(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SelectionChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) BrandBlue else BrandSurface)
            .border(
                width = 1.dp,
                color = if (selected) BrandBlue else BrandLightBlue,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = title,
                color = if (selected) Color.White else TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) BrandBlue else BrandBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}
