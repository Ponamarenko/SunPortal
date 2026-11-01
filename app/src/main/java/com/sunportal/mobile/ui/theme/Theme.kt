package com.sunportal.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = svitgrid_primary,
    onPrimary = svitgrid_onPrimary,
    primaryContainer = svitgrid_primaryContainer,
    onPrimaryContainer = svitgrid_onPrimaryContainer,
    secondary = svitgrid_secondary,
    onSecondary = svitgrid_onSecondary,
    secondaryContainer = svitgrid_secondaryContainer,
    onSecondaryContainer = svitgrid_onSecondaryContainer,
    tertiary = svitgrid_tertiary,
    onTertiary = svitgrid_onTertiary,
    tertiaryContainer = svitgrid_tertiaryContainer,
    onTertiaryContainer = svitgrid_onTertiaryContainer,
    error = svitgrid_error,
    onError = svitgrid_onError,
    errorContainer = svitgrid_errorContainer,
    onErrorContainer = svitgrid_onErrorContainer,
    outline = svitgrid_outline,
    background = svitgrid_background,
    onBackground = svitgrid_onBackground,
    surface = svitgrid_surface,
    onSurface = svitgrid_onSurface,
    surfaceVariant = svitgrid_surfaceVariant,
    onSurfaceVariant = svitgrid_onSurfaceVariant,
    inverseSurface = svitgrid_inverseSurface,
    inverseOnSurface = svitgrid_inverseOnSurface,
    inversePrimary = svitgrid_inversePrimary,
    surfaceTint = svitgrid_surfaceTint,
    outlineVariant = svitgrid_outlineVariant,
    scrim = svitgrid_scrim,
)

private val DarkColorScheme = darkColorScheme(
    primary = svitgrid_inversePrimary,
    onPrimary = svitgrid_onPrimaryContainer,
    primaryContainer = svitgrid_primaryContainer,
    onPrimaryContainer = svitgrid_onPrimaryContainer,
    secondary = svitgrid_secondary,
    onSecondary = svitgrid_onSecondary,
    secondaryContainer = svitgrid_secondaryContainer,
    onSecondaryContainer = svitgrid_onSecondaryContainer,
    tertiary = svitgrid_tertiary,
    onTertiary = svitgrid_onTertiary,
    tertiaryContainer = svitgrid_tertiaryContainer,
    onTertiaryContainer = svitgrid_onTertiaryContainer,
    error = svitgrid_error,
    onError = svitgrid_onError,
    errorContainer = svitgrid_errorContainer,
    onErrorContainer = svitgrid_onErrorContainer,
    outline = svitgrid_outline,
    background = svitgrid_background,
    onBackground = svitgrid_onBackground,
    surface = svitgrid_surface,
    onSurface = svitgrid_onSurface,
    surfaceVariant = svitgrid_surfaceVariant,
    onSurfaceVariant = svitgrid_onSurfaceVariant,
    inverseSurface = svitgrid_inverseSurface,
    inverseOnSurface = svitgrid_inverseOnSurface,
    inversePrimary = svitgrid_primary,
    surfaceTint = svitgrid_surfaceTint,
    outlineVariant = svitgrid_outlineVariant,
    scrim = svitgrid_scrim,
)

@Composable
fun SunPortalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}