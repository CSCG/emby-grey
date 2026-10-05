package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EmbyStreamColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004D53),
    onPrimaryContainer = Color(0xFF80F5FF),
    secondary = AccentEmerald,
    onSecondary = Color(0xFF003918),
    secondaryContainer = Color(0xFF005324),
    onSecondaryContainer = Color(0xFF6BFF9E),
    tertiary = AccentPurple,
    onTertiary = Color.White,
    background = CinemaBlack,
    onBackground = TextPrimary,
    surface = CinemaDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CinemaSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CinemaCardBorder,
    outlineVariant = Color(0xFF1E2638),
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EmbyStreamColorScheme,
        typography = Typography,
        content = content
    )
}
