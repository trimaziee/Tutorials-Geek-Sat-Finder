package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = SpaceBlack,
    primaryContainer = CardSurfaceVariant,
    onPrimaryContainer = CyanAccent,
    secondary = EmeraldLock,
    onSecondary = SpaceBlack,
    secondaryContainer = CardSurfaceVariant,
    onSecondaryContainer = EmeraldLock,
    tertiary = GoldSat,
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = DeepSpace,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderCyan,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
