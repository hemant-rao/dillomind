package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

private val DarkColorScheme = darkColorScheme(
    primary = XelloTealPrimary,
    secondary = XelloDarkSecondary,
    tertiary = XelloTealGlow,
    background = XelloDarkBg,
    surface = XelloDarkSurface,
    onPrimary = Color.White,
    onSecondary = XelloDarkText,
    onBackground = XelloDarkText,
    onSurface = XelloDarkText,
    primaryContainer = XelloTealDark,
    onPrimaryContainer = Color.White,
    secondaryContainer = XelloDarkSecondary,
    onSecondaryContainer = XelloDarkText
)

private val LightColorScheme = lightColorScheme(
    primary = XelloTealDark,
    secondary = XelloLightSecondary,
    tertiary = XelloTealPrimary,
    background = XelloLightBg,
    surface = XelloLightSurface,
    onPrimary = Color.White,
    onSecondary = XelloLightText,
    onBackground = XelloLightText,
    onSurface = XelloLightText,
    primaryContainer = XelloTealLight,
    onPrimaryContainer = XelloTealDark,
    secondaryContainer = XelloLightSecondary,
    onSecondaryContainer = XelloLightText
)

fun getDynamicColorScheme(isDark: Boolean, accent: String): androidx.compose.material3.ColorScheme {
    return when (accent) {
        "OCEAN" -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFF38BDF8),
                    secondary = Color(0xFF1E293B),
                    tertiary = Color(0xFF7DD3FC),
                    background = Color(0xFF030712),
                    surface = Color(0xFF0F172A),
                    onPrimary = Color.Black,
                    onSecondary = Color(0xFFF8FAFC),
                    onBackground = Color(0xFFF8FAFC),
                    onSurface = Color(0xFFF8FAFC),
                    primaryContainer = Color(0xFF0369A1),
                    onPrimaryContainer = Color.White,
                    secondaryContainer = Color(0xFF1E293B),
                    onSecondaryContainer = Color(0xFFF8FAFC)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF0369A1),
                    secondary = Color(0xFFE0F2FE),
                    tertiary = Color(0xFF0284C7),
                    background = Color(0xFFF8FAFC),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color.White,
                    onSecondary = Color(0xFF0F172A),
                    onBackground = Color(0xFF0F172A),
                    onSurface = Color(0xFF0F172A),
                    primaryContainer = Color(0xFFBAE6FD),
                    onPrimaryContainer = Color(0xFF0369A1),
                    secondaryContainer = Color(0xFFE0F2FE),
                    onSecondaryContainer = Color(0xFF0F172A)
                )
            }
        }
        "VIOLET" -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFC084FC),
                    secondary = Color(0xFF2E1065),
                    tertiary = Color(0xFFE9D5FF),
                    background = Color(0xFF090514),
                    surface = Color(0xFF180E29),
                    onPrimary = Color.Black,
                    onSecondary = Color(0xFFFAF5FF),
                    onBackground = Color(0xFFFAF5FF),
                    onSurface = Color(0xFFFAF5FF),
                    primaryContainer = Color(0xFF6D28D9),
                    onPrimaryContainer = Color.White,
                    secondaryContainer = Color(0xFF2E1065),
                    onSecondaryContainer = Color(0xFFFAF5FF)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF6D28D9),
                    secondary = Color(0xFFF3E8FF),
                    tertiary = Color(0xFF7C3AED),
                    background = Color(0xFFFAF5FF),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color.White,
                    onSecondary = Color(0xFF1E1B4B),
                    onBackground = Color(0xFF1E1B4B),
                    onSurface = Color(0xFF1E1B4B),
                    primaryContainer = Color(0xFFE9D5FF),
                    onPrimaryContainer = Color(0xFF6D28D9),
                    secondaryContainer = Color(0xFFF3E8FF),
                    onSecondaryContainer = Color(0xFF1E1B4B)
                )
            }
        }
        "AMBER" -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFF59E0B),
                    secondary = Color(0xFF451A03),
                    tertiary = Color(0xFFFCD34D),
                    background = Color(0xFF0C0702),
                    surface = Color(0xFF1E1005),
                    onPrimary = Color.Black,
                    onSecondary = Color(0xFFFFFDF5),
                    onBackground = Color(0xFFFFFDF5),
                    onSurface = Color(0xFFFFFDF5),
                    primaryContainer = Color(0xFFB45309),
                    onPrimaryContainer = Color.White,
                    secondaryContainer = Color(0xFF451A03),
                    onSecondaryContainer = Color(0xFFFFFDF5)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFB45309),
                    secondary = Color(0xFFFEF3C7),
                    tertiary = Color(0xFFD97706),
                    background = Color(0xFFFFFDF5),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color.White,
                    onSecondary = Color(0xFF291A09),
                    onBackground = Color(0xFF291A09),
                    onSurface = Color(0xFF291A09),
                    primaryContainer = Color(0xFFFDE68A),
                    onPrimaryContainer = Color(0xFFB45309),
                    secondaryContainer = Color(0xFFFEF3C7),
                    onSecondaryContainer = Color(0xFF291A09)
                )
            }
        }
        "ROSE" -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFFB7185),
                    secondary = Color(0xFF4C0519),
                    tertiary = Color(0xFFFDA4AF),
                    background = Color(0xFF0F040A),
                    surface = Color(0xFF240C16),
                    onPrimary = Color.Black,
                    onSecondary = Color(0xFFFFF0F2),
                    onBackground = Color(0xFFFFF0F2),
                    onSurface = Color(0xFFFFF0F2),
                    primaryContainer = Color(0xFFBE123C),
                    onPrimaryContainer = Color.White,
                    secondaryContainer = Color(0xFF4C0519),
                    onSecondaryContainer = Color(0xFFFFF0F2)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFBE123C),
                    secondary = Color(0xFFFFE4E6),
                    tertiary = Color(0xFFE11D48),
                    background = Color(0xFFFFF0F2),
                    surface = Color(0xFFFFFFFF),
                    onPrimary = Color.White,
                    onSecondary = Color(0xFF0F040A),
                    onBackground = Color(0xFF0F040A),
                    onSurface = Color(0xFF0F040A),
                    primaryContainer = Color(0xFFFECDD3),
                    onPrimaryContainer = Color(0xFFBE123C),
                    secondaryContainer = Color(0xFFFFE4E6),
                    onSecondaryContainer = Color(0xFF0F040A)
                )
            }
        }
        else -> { // "TEAL"
            if (isDark) DarkColorScheme else LightColorScheme
        }
    }
}

fun getDynamicTypography(fontStyle: String, fontScale: String): Typography {
    val family = when (fontStyle) {
        "SERIF" -> FontFamily.Serif
        "MONO" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
    val scaleFactor = when (fontScale) {
        "COMPACT" -> 0.85f
        "EXPANDED" -> 1.15f
        else -> 1.0f
    }
    
    return Typography(
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (22 * scaleFactor).sp,
            lineHeight = (28 * scaleFactor).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (18 * scaleFactor).sp,
            lineHeight = (24 * scaleFactor).sp,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scaleFactor).sp,
            lineHeight = (24 * scaleFactor).sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scaleFactor).sp,
            lineHeight = (20 * scaleFactor).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scaleFactor).sp,
            lineHeight = (16 * scaleFactor).sp,
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (14 * scaleFactor).sp,
            lineHeight = (20 * scaleFactor).sp,
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (12 * scaleFactor).sp,
            lineHeight = (16 * scaleFactor).sp,
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scaleFactor).sp,
            lineHeight = (16 * scaleFactor).sp,
            letterSpacing = 0.5.sp
        )
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled dynamic system color to preserve our premium hand-crafted brand theme
    accent: String = "TEAL",
    typographyStyle: String = "SANS",
    fontScale: String = "BALANCED",
    content: @Composable () -> Unit,
) {
    val colorScheme = getDynamicColorScheme(darkTheme, accent)
    val typography = getDynamicTypography(typographyStyle, fontScale)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
