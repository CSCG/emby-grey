package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ContentFirstDarkColorScheme = darkColorScheme(
    primary = AppAccent,
    onPrimary = Color(0xFF04191C),
    primaryContainer = AppAccentSubtle,
    onPrimaryContainer = AppTextPrimary,
    secondary = AppAccent,
    onSecondary = Color(0xFF04191C),
    secondaryContainer = AppElevatedSurface,
    onSecondaryContainer = AppTextPrimary,
    tertiary = AppTextSecondary,
    onTertiary = AppBackground,
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = AppElevatedSurface,
    onSurfaceVariant = AppTextSecondary,
    outline = AppDivider,
    outlineVariant = AppSelectedSurface,
    error = AppLiveRed,
    onError = Color.White
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), // badges
    small = RoundedCornerShape(6.dp),      // minor tags
    medium = RoundedCornerShape(8.dp),     // cards, artwork, buttons
    large = RoundedCornerShape(12.dp),     // hero banners
    extraLarge = RoundedCornerShape(16.dp) // sheets, dialogs
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ContentFirstDarkColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
