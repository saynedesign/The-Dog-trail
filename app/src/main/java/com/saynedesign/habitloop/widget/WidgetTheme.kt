package com.saynedesign.habitloop.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider as SingleColorProvider

/**
 * Shared day/night color providers for all Glance widgets so they follow the
 * device light/dark mode instead of hardcoding the dark palette.
 * Mirrors the in-app theme (ui/theme/Color.kt).
 */
object WidgetTheme {
    // Backgrounds / surfaces
    val bg = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1C202B))
    val surface = ColorProvider(day = Color(0xFFEEF1FF), night = Color(0xFF292E3B))

    // Text
    val textPrimary = ColorProvider(day = Color(0xFF1D1B20), night = Color(0xFFFFFFFF))
    val textSecondary = ColorProvider(day = Color(0xFF757575), night = Color(0xFF8B93A6))

    // Brand accents (constant across modes)
    val accent = SingleColorProvider(Color(0xFF4B68FF))
    val accentDone = SingleColorProvider(Color(0xFF3366FF))
    val onAccent = SingleColorProvider(Color(0xFFFFFFFF))
}
