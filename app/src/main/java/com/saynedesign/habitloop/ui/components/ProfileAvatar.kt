package com.saynedesign.habitloop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * The one true profile avatar. Shows the user's picture if set; otherwise a
 * consistent placeholder — their name initials, or a person icon when the name
 * is blank. Use this everywhere a profile picture is expected so the fallback
 * never differs screen-to-screen.
 */
@Composable
fun ProfileAvatar(
    imageUri: String?,
    name: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    contentColor: Color = MaterialTheme.colorScheme.primary,
    initialsFontSize: TextUnit = 18.sp
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUri.isNullOrBlank()) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = profileInitials(name)
            if (initials.isNotBlank()) {
                Text(
                    text = initials,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = initialsFontSize
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = contentColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }
        }
    }
}

/** First letters of up to the first two words of the name (e.g. "Aman Kumar" -> "AK"). */
fun profileInitials(name: String): String =
    name.trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
