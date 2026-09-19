package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexoraColorScheme = darkColorScheme(
    primary = NexoraCyanBright,
    onPrimary = Color(0xFF041E28),
    primaryContainer = Color(0xFF0E3D4F),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = NexoraViolet,
    onSecondary = Color(0xFF1E1035),
    secondaryContainer = Color(0xFF3B1D66),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = NexoraMagenta,
    onTertiary = Color(0xFF380720),
    tertiaryContainer = Color(0xFF6B1740),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = NexoraDarkBg,
    onBackground = NexoraTextPrimary,
    surface = NexoraSurfaceDark,
    onSurface = NexoraTextPrimary,
    surfaceVariant = NexoraSurfaceElevated,
    onSurfaceVariant = NexoraTextSecondary,
    outline = NexoraGlassBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode for futuristic glassmorphism vibe
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexoraColorScheme,
        typography = Typography,
        content = content
    )
}
