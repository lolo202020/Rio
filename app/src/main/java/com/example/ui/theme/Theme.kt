package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CinemaColorScheme = darkColorScheme(
    primary = CinemaGold,
    onPrimary = CinemaBackground,
    primaryContainer = CinemaGoldDark,
    onPrimaryContainer = CinemaTextPrimary,
    secondary = AnamorphicBlue,
    onSecondary = CinemaBackground,
    secondaryContainer = LensIndigo,
    onSecondaryContainer = CinemaTextPrimary,
    tertiary = CinemaRed,
    onTertiary = CinemaTextPrimary,
    background = CinemaBackground,
    onBackground = CinemaTextPrimary,
    surface = CinemaSurface,
    onSurface = CinemaTextPrimary,
    surfaceVariant = CinemaSurfaceElevated,
    onSurfaceVariant = CinemaTextSecondary,
    outline = CinemaSurfaceBorder,
    error = CinemaRed
)

@Composable
fun RioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinemaColorScheme,
        typography = Typography,
        content = content
    )
}
