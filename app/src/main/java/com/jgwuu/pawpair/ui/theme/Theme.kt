package com.jgwuu.pawpair.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// FOREST (Default Pastel Green)
private val ForestDarkColorScheme = darkColorScheme(
    primary = PastelPrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E4E23),
    onPrimaryContainer = PastelGreenContainer,
    secondary = PastelSecondaryPeach,
    onSecondary = PastelOnSecondary,
    secondaryContainer = Color(0xFF382921),
    onSecondaryContainer = PastelSecondaryPeach,
    background = Color(0xFF1A1D1A),
    onBackground = PastelBackground,
    surface = Color(0xFF252A24),
    onSurface = PastelBackground,
    surfaceVariant = Color(0xFF2F352E),
    onSurfaceVariant = PastelSurfaceVariant,
    outline = PastelOutline
)

private val ForestLightColorScheme = lightColorScheme(
    primary = PastelPrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PastelGreenContainer,
    onPrimaryContainer = PastelOnGreenContainer,
    secondary = PastelSecondaryPeach,
    onSecondary = PastelOnSecondary,
    secondaryContainer = PastelSecondaryPeach,
    onSecondaryContainer = PastelOnSecondary,
    background = PastelBackground,
    onBackground = PastelOnSecondary,
    surface = PastelSurface,
    onSurface = PastelOnSecondary,
    surfaceVariant = PastelSurfaceVariant,
    onSurfaceVariant = PastelOnSurfaceVariant,
    outline = PastelOutline
)

// SUNSET (Warm Orange / Coral / Peach)
private val SunsetLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF8A65),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFCCBC),
    onPrimaryContainer = Color(0xFFBF360C),
    secondary = Color(0xFFFFD54F),
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFFFFF9C4),
    onSecondaryContainer = Color(0xFF4F3A00),
    background = Color(0xFFFFFBF8),
    onBackground = Color(0xFF3E2723),
    surface = Color.White,
    onSurface = Color(0xFF3E2723),
    surfaceVariant = Color(0xFFFBE9E7),
    onSurfaceVariant = Color(0xFF5D4037),
    outline = Color(0xFFFFAB91)
)

private val SunsetDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFAB91),
    onPrimary = Color(0xFF4E1D00),
    primaryContainer = Color(0xFF7E2A0B),
    onPrimaryContainer = Color(0xFFFFCCBC),
    secondary = Color(0xFFFFE082),
    onSecondary = Color(0xFF3F2D00),
    background = Color(0xFF211714),
    onBackground = Color(0xFFEDE0DB),
    surface = Color(0xFF2E201C),
    onSurface = Color(0xFFEDE0DB),
    surfaceVariant = Color(0xFF3E2D27),
    onSurfaceVariant = Color(0xFFD3C4BE),
    outline = Color(0xFF8D6E63)
)

// OCEAN (Teal / Cyan / Blue)
private val OceanLightColorScheme = lightColorScheme(
    primary = Color(0xFF26A69A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF004D40),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF0D47A1),
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF0D47A1),
    background = Color(0xFFF5FBFC),
    onBackground = Color(0xFF00332C),
    surface = Color.White,
    onSurface = Color(0xFF00332C),
    surfaceVariant = Color(0xFFE0F2F1),
    onSurfaceVariant = Color(0xFF00695C),
    outline = Color(0xFF80CBC4)
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00332C),
    primaryContainer = Color(0xFF00564D),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF002B71),
    background = Color(0xFF131F20),
    onBackground = Color(0xFFE0F2F1),
    surface = Color(0xFF1B2B2C),
    onSurface = Color(0xFFE0F2F1),
    surfaceVariant = Color(0xFF263D3F),
    onSurfaceVariant = Color(0xFFB2DFDB),
    outline = Color(0xFF4DB6AC)
)

// CANDY (Soft Pink / Violet)
private val CandyLightColorScheme = lightColorScheme(
    primary = Color(0xFFEC407A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF8BBD0),
    onPrimaryContainer = Color(0xFF880E4F),
    secondary = Color(0xFFAB47BC),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1BEE7),
    onSecondaryContainer = Color(0xFF4A148C),
    background = Color(0xFFFDF8FA),
    onBackground = Color(0xFF3E1F2C),
    surface = Color.White,
    onSurface = Color(0xFF3E1F2C),
    surfaceVariant = Color(0xFFFCE4EC),
    onSurfaceVariant = Color(0xFFAD1457),
    outline = Color(0xFFF48FB1)
)

private val CandyDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF48FB1),
    onPrimary = Color(0xFF5C0028),
    primaryContainer = Color(0xFF880E4F),
    onPrimaryContainer = Color(0xFFF8BBD0),
    secondary = Color(0xFFCE93D8),
    onSecondary = Color(0xFF38004D),
    background = Color(0xFF23151B),
    onBackground = Color(0xFFFCE4EC),
    surface = Color(0xFF301E26),
    onSurface = Color(0xFFFCE4EC),
    surfaceVariant = Color(0xFF402833),
    onSurfaceVariant = Color(0xFFF8BBD0),
    outline = Color(0xFFEC407A)
)

// CYBER (Neon Violet / Dark Sci-Fi)
private val CyberLightColorScheme = lightColorScheme(
    primary = Color(0xFF7C4DFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1C4E9),
    onPrimaryContainer = Color(0xFF311B92),
    secondary = Color(0xFF00E5FF),
    onSecondary = Color(0xFF003840),
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    background = Color(0xFFF6F5FB),
    onBackground = Color(0xFF1E1B2C),
    surface = Color.White,
    onSurface = Color(0xFF1E1B2C),
    surfaceVariant = Color(0xFFEDE7F6),
    onSurfaceVariant = Color(0xFF512DA8),
    outline = Color(0xFFB39DDB)
)

private val CyberDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB388FF),
    onPrimary = Color(0xFF22006B),
    primaryContainer = Color(0xFF4A148C),
    onPrimaryContainer = Color(0xFFE1BEE7),
    secondary = Color(0xFF18FFFF),
    onSecondary = Color(0xFF003840),
    background = Color(0xFF0F0C1B),
    onBackground = Color(0xFFEDE7F6),
    surface = Color(0xFF181429),
    onSurface = Color(0xFFEDE7F6),
    surfaceVariant = Color(0xFF251E3E),
    onSurfaceVariant = Color(0xFFD1C4E9),
    outline = Color(0xFF7C4DFF)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    appTheme: String = "FOREST",
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        appTheme == "SUNSET" -> if (darkTheme) SunsetDarkColorScheme else SunsetLightColorScheme
        appTheme == "OCEAN" -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        appTheme == "CANDY" -> if (darkTheme) CandyDarkColorScheme else CandyLightColorScheme
        appTheme == "CYBER" -> if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme
        else -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
