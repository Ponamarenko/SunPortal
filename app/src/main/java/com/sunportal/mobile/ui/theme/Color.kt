package com.sunportal.mobile.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// Svitgrid Color Scheme
val svitgrid_primary = Color(0xFF00668A)
val svitgrid_onPrimary = Color.White
val svitgrid_primaryContainer = Color(0xFFBAE6FF)
val svitgrid_onPrimaryContainer = Color(0xFF001F29)
val svitgrid_secondary = Color(0xFF4A6B7F)
val svitgrid_onSecondary = Color.White
val svitgrid_secondaryContainer = Color(0xFFD0E5F1)
val svitgrid_onSecondaryContainer = Color(0xFF082029)
val svitgrid_tertiary = Color(0xFF7A5340)
val svitgrid_onTertiary = Color.White
val svitgrid_tertiaryContainer = Color(0xFFFFDBCF)
val svitgrid_onTertiaryContainer = Color(0xFF2C160B)
val svitgrid_error = Color(0xFFBA1A1A)
val svitgrid_onError = Color.White
val svitgrid_errorContainer = Color(0xFFFFDAD6)
val svitgrid_onErrorContainer = Color(0xFF410002)
val svitgrid_outline = Color(0xFF6F797B)
val svitgrid_background = Color(0xFFFBFDFF)
val svitgrid_onBackground = Color(0xFF191C1D)
val svitgrid_surface = Color(0xFFFBFDFF)
val svitgrid_onSurface = Color(0xFF191C1D)
val svitgrid_surfaceVariant = Color(0xDFE3E5)
val svitgrid_onSurfaceVariant = Color(040F1113)
val svitgrid_inverseSurface = Color(0xFF2E3133)
val svitgrid_inverseOnSurface = Color(0xF0F1F3)
val svitgrid_inversePrimary = Color(0xFF7DD9FF)
val svitgrid_surfaceTint = Color(0xFF00668A)
val svitgrid_outlineVariant = Color(0xBFC8CB)
val svitgrid_scrim = Color(0xFF000000)

// Svitgrid Solar Colors
val SolarYellow = Color(0xFFFFC107)
val SolarOrange = Color(0xFFFF9800)
val SolarBlue = Color(0xFF2196F3)
val BatteryGreen = Color(0xFF4CAF50)
val BatteryOrange = Color(0xFFFF9800)
val BatteryRed = Color(0xFFF44336)
val GridGreen = Color(0xFF4CAF50)
val GridRed = Color(0xFFF44336)
val LoadPurple = Color(0xFF9C27B0)

// Extended color scheme for Svitgrid
val ColorScheme.svitgridPrimary
    get() = svitgrid_primary
    
val ColorScheme.svitgridOnPrimary
    get() = svitgrid_onPrimary
    
val ColorScheme.svitgridPrimaryContainer
    get() = svitgrid_primaryContainer
    
val ColorScheme.svitgridOnPrimaryContainer
    get() = svitgrid_onPrimaryContainer
    
val ColorScheme.solarPrimary
    get() = SolarYellow
    
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