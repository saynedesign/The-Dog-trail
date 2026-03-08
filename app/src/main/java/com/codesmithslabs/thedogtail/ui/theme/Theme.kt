package com.codesmithslabs.thedogtail.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlueLight,
    secondary = BrandPurple,
    tertiary = BrandBlueDark,
    background = PremiumDarkBackground, // Global Premium Dark Background
    surface = PremiumDarkSurface, // Global Premium Dark Surface
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = TextPrimary,
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = PremiumDarkTextMuted,
    outlineVariant = PremiumDarkBorder,
    error = ErrorRed,
    errorContainer = PremiumLogoutBgDark,
    onError = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    secondary = BrandPurple,
    tertiary = BrandLightBlue,
    background = LightBackground,
    surface = WhiteColor,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outlineVariant = BrandLightBlue,
    errorContainer = Color(0xFFFFEBEE), // Subtle light red for light mode logout
    error = ErrorRed,
    onError = Color(0xFFFFFFFF)
)

@Composable
fun TheDogTailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to enforce brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb() // Transparent for edge-to-edge
            // isAppearanceLightStatusBars = !darkTheme -> If dark theme, icons are light (false = light status bars? No. true = light status bars = dark icons)
            // darkTheme = false (Light Mode) -> isAppearanceLightStatusBars = true (Dark Icons)
            // darkTheme = true (Dark Mode) -> isAppearanceLightStatusBars = false (Light Icons)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
