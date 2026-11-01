package com.deye.mobile.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// Light theme colors
val md_theme_light_primary = Color(0xFF00668A)
val md_theme_light_onPrimary = Color.White
val md_theme_light_primaryContainer = Color(0xFFBAE6FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001F29)
val md_theme_light_secondary = Color(0xFF4A6B7F)
val md_theme_light_onSecondary = Color.White
val md_theme_light_secondaryContainer = Color(0xFFD0E5F1)
val md_theme_light_onSecondaryContainer = Color(0xFF082029)
val md_theme_light_tertiary = Color(0xFF7A5340)
val md_theme_light_onTertiary = Color.White
val md_theme_light_tertiaryContainer = Color(0xFFFFDBCF)
val md_theme_light_onTertiaryContainer = Color(0xFF2C160B)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color.White
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_outline = Color(0xFF6F797B)
val md_theme_light_background = Color(0xFFFBFDFF)
val md_theme_light_onBackground = Color(0xFF191C1D)
val md_theme_light_surface = Color(0xFFFBFDFF)
val md_theme_light_onSurface = Color(0xFF191C1D)
val md_theme_light_surfaceVariant = Color(0xDFE3E5)
val md_theme_light_onSurfaceVariant = Color(040F1113)
val md_theme_light_inverseSurface = Color(0xFF2E3133)
val md_theme_light_inverseOnSurface = Color(0xF0F1F3)
val md_theme_light_inversePrimary = Color(0xFF7DD9FF)
val md_theme_light_surfaceTint = Color(0xFF00668A)
val md_theme_light_outlineVariant = Color(0xBFC8CB)
val md_theme_light_scrim = Color(0xFF000000)

// Dark theme colors
val md_theme_dark_primary = Color(0xFF7DD9FF)
val md_theme_dark_onPrimary = Color(0xFF003850)
val md_theme_dark_primaryContainer = Color(0xFF004B73)
val md_theme_dark_onPrimaryContainer = Color(0xFFBAE6FF)
val md_theme_dark_secondary = Color(0xFFB0CAD5)
val md_theme_dark_onSecondary = Color(0xFF1C343E)
val md_theme_dark_secondaryContainer = Color(0xFF334A56)
val md_theme_dark_onSecondaryContainer = Color(0xFFD0E5F1)
val md_theme_dark_tertiary = Color(0xFFE6C1B3)
val md_theme_dark_onTertiary = Color(0BF43211)
val md_theme_dark_tertiaryContainer = Color(0xFF5F3D2E)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFDBCF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_outline = Color(0x8F9CA1A3)
val md_theme_dark_background = Color(0xFF191C1D)
val md_theme_dark_onBackground = Color(0xE0E1E3)
val md_theme_dark_surface = Color(0xFF191C1D)
val md_theme_dark_onSurface = Color(0xE0E1E3)
val md_theme_dark_surfaceVariant = Color(0xDFE3E5)
val md_theme_dark_onSurfaceVariant = Color(0xBFC8CB)
val md_theme_dark_inverseSurface = Color(0xE0E1E3)
val md_theme_dark_inverseOnSurface = Color(0xFF191C1D)
val md_theme_dark_inversePrimary = Color(0xFF00668A)
val md_theme_dark_surfaceTint = Color(0xFF7DD9FF)
val md_theme_dark_outlineVariant = Color(0x4F57595B)
val md_theme_dark_scrim = Color(0xFF000000)

// Custom colors for solar monitoring
val SolarYellow = Color(0xFFFFC107)
val SolarOrange = Color(0xFFFF9800)
val SolarBlue = Color(0xFF2196F3)
val BatteryGreen = Color(0xFF4CAF50)
val BatteryOrange = Color(0xFFFF9800)
val BatteryRed = Color(0xFFF44336)
val GridGreen = Color(0xFF4CAF50)
val GridRed = Color(0xFFF44336)
val LoadPurple = Color(0xFF9C27B0)

// Extended color scheme
val ColorScheme.solarPrimary
    get() = if (isLight) SolarYellow else Color(0xFFFFD54F)
    
val ColorScheme.batteryGood
    get() = BatteryGreen
    
val ColorScheme.batteryMedium
    get() = BatteryOrange
    
val ColorScheme.batteryLow
    get() = BatteryRed
    
val ColorScheme.gridExport
    get() = GridGreen
    
val ColorScheme.gridImport
    get() = GridRed
    
val ColorScheme.loadPower
    get() = LoadPurple