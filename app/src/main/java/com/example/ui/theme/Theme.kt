package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ForestColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = ForestSecondary,
    onSecondary = ForestOnSecondary,
    secondaryContainer = ForestSecondaryContainer,
    onSecondaryContainer = ForestOnSecondaryContainer,
    tertiary = ForestTertiary,
    onTertiary = ForestOnPrimary,
    tertiaryContainer = ForestTertiaryContainer,
    onTertiaryContainer = ForestTertiaryFixed,
    background = ForestBackground,
    onBackground = ForestOnBackground,
    surface = ForestSurface,
    onSurface = ForestOnSurface,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = ForestOnSurfaceVariant,
    surfaceContainerLowest = ForestSurfaceContainerLowest,
    surfaceContainerLow = ForestSurfaceContainerLow,
    surfaceContainer = ForestSurfaceContainer,
    surfaceContainerHigh = ForestSurfaceContainerHigh,
    surfaceContainerHighest = ForestSurfaceContainerHighest,
    outline = ForestOutline,
    outlineVariant = ForestOutlineVariant,
    error = ForestError,
    onError = ForestOnError,
    errorContainer = ForestErrorContainer,
    onErrorContainer = ForestOnErrorContainer
)

@Composable
fun CashManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ForestColorScheme,
        typography = Typography,
        content = content
    )
}
