package com.codesmithslabs.thedogtail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandLightBlue
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary

@Composable
fun CircularProgress(
    percentage: Float,
    modifier: Modifier = Modifier,
    radius: Dp = 40.dp,
    strokeWidth: Dp = 8.dp,
    color: Color = BrandBlue,
    backgroundColor: Color = BrandLightBlue,
    content: @Composable () -> Unit = {}
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(radius * 2)) {
        Canvas(modifier = Modifier.size(radius * 2)) {
            // Draw background circle
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Draw progress circle
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360 * percentage,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        content()
    }
}

@Composable
fun HabitLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    valueLabel: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null || valueLabel != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                if (valueLabel != null) {
                    Text(
                        text = valueLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = BrandBlue,
            trackColor = BrandLightBlue,
            strokeCap = StrokeCap.Round,
        )
    }
}
