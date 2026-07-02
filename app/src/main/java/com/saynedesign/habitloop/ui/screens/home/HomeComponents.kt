package com.saynedesign.habitloop.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.saynedesign.habitloop.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val dates = remember {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("d", Locale.getDefault())
        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        (-30..14).map { offset -> // Show a window of past 30 days to next 14 days
            val date = today.plusDays(offset.toLong())
            Triple(
                date.format(dayFormatter),
                date.format(dateFormatter),
                date
            )
        }
    }

    val listState = rememberLazyListState()

    // Scroll to selected date on launch
    LaunchedEffect(selectedDate) {
        val targetIndex = dates.indexOfFirst { it.third == selectedDate }.takeIf { it != -1 }
            ?: dates.indexOfFirst { it.third == LocalDate.now() }
        
        if (targetIndex != -1) {
            listState.animateScrollToItem((targetIndex - 2).coerceAtLeast(0))
        }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(dates) { (dayStr, dateStr, localDate) ->
            val isSelected = localDate == selectedDate
            val activeColor = Color(0xFF4B68FF)
            
            val containerColor = if (isSelected) {
                activeColor
            } else {
                if (isDark) Color(0xFF1C202B) else Color(0xFFF5F6FA)
            }
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .width(56.dp)
                    .clickable { onDateSelected(localDate) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayStr.take(3).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else (if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else (if (isDark) Color.White else Color.Black)
                    )
                    
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(10.dp)) // Maintain same height alignment
                    }
                }
            }
        }
    }
}

