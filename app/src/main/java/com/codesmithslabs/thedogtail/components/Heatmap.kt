package com.codesmithslabs.thedogtail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandLightBlue
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary

@Composable
fun YearlyGrid(
    data: List<Int>, // 0 to 4 intensity
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Month labels (Simplified)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Jan", "May", "Aug", "Dec").forEach { month ->
                Text(
                    text = month,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth().height(200.dp) // Approximate height
        ) {
            items(data.size) { index ->
                HeatmapCell(intensity = data[index])
            }
        }
        
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(end = 4.dp)
            )
            (0..4).forEach { intensity ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(getColorForIntensity(intensity))
                )
            }
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun HeatmapCell(intensity: Int) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp))
            .background(getColorForIntensity(intensity))
    )
}

private fun getColorForIntensity(intensity: Int): Color {
    return when (intensity) {
        0 -> BrandLightBlue
        1 -> BrandBlue.copy(alpha = 0.25f)
        2 -> BrandBlue.copy(alpha = 0.5f)
        3 -> BrandBlue.copy(alpha = 0.75f)
        4 -> BrandBlue
        else -> BrandLightBlue
    }
}
