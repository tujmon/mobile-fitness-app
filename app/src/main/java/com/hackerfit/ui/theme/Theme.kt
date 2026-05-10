package com.hackerfit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ShadBackground = Color(0xFFFFFFFF)
val ShadForeground = Color(0xFF0A0A0A)
val ShadCard = Color(0xFFFFFFFF)
val ShadCardForeground = Color(0xFF0A0A0A)
val ShadPrimary = Color(0xFF171717)
val ShadPrimaryForeground = Color(0xFFFAFAFA)
val ShadSecondary = Color(0xFFF5F5F5)
val ShadSecondaryForeground = Color(0xFF171717)
val ShadMuted = Color(0xFFF5F5F5)
val ShadMutedForeground = Color(0xFF737373)
val ShadAccent = Color(0xFFF5F5F5)
val ShadAccentForeground = Color(0xFF171717)
val ShadDestructive = Color(0xFFEF4444)
val ShadDestructiveForeground = Color(0xFFFAFAFA)
val ShadBorder = Color(0xFFE5E5E5)
val ShadInput = Color(0xFFE5E5E5)
val ShadRing = Color(0xFFA3A3A3)

val ShadChart1 = Color(0xFFF97316)
val ShadChart2 = Color(0xFF3B82F6)
val ShadChart3 = Color(0xFF6366F1)
val ShadChart4 = Color(0xFFFACC15)
val ShadChart5 = Color(0xFFF59E0B)

val ShadOrangeContainer = Color(0xFFFFF7ED)
val ShadOnOrangeContainer = Color(0xFF7C2D12)
val ShadOrangeDarkContainer = Color(0xFF7C2D12)
val ShadOnOrangeDarkContainer = Color(0xFFFED7AA)

val ShadDarkBackground = Color(0xFF0A0A0A)
val ShadDarkForeground = Color(0xFFFAFAFA)
val ShadDarkCard = Color(0xFF171717)
val ShadDarkCardForeground = Color(0xFFFAFAFA)
val ShadDarkPrimary = Color(0xFFE5E5E5)
val ShadDarkPrimaryForeground = Color(0xFF171717)
val ShadDarkSecondary = Color(0xFF262626)
val ShadDarkSecondaryForeground = Color(0xFFFAFAFA)
val ShadDarkMuted = Color(0xFF262626)
val ShadDarkMutedForeground = Color(0xFFA3A3A3)
val ShadDarkAccent = Color(0xFF262626)
val ShadDarkAccentForeground = Color(0xFFFAFAFA)
val ShadDarkDestructive = Color(0xFFEF4444)
val ShadDarkBorder = Color(0xFF262626)
val ShadDarkInput = Color(0xFF262626)
val ShadDarkRing = Color(0xFF737373)

private val LightColorScheme = lightColorScheme(
    primary = ShadPrimary,
    onPrimary = ShadPrimaryForeground,
    primaryContainer = ShadMuted,
    onPrimaryContainer = ShadSecondaryForeground,
    secondary = ShadSecondary,
    onSecondary = ShadSecondaryForeground,
    secondaryContainer = ShadBorder,
    onSecondaryContainer = ShadSecondaryForeground,
    tertiary = ShadChart1,
    onTertiary = Color.White,
    tertiaryContainer = ShadOrangeContainer,
    onTertiaryContainer = ShadOnOrangeContainer,
    error = ShadDestructive,
    onError = ShadDestructiveForeground,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    surface = ShadBackground,
    onSurface = ShadForeground,
    surfaceVariant = ShadMuted,
    onSurfaceVariant = ShadMutedForeground,
    outline = ShadBorder,
    outlineVariant = ShadBorder,
    background = ShadBackground,
    onBackground = ShadForeground
)

private val DarkColorScheme = darkColorScheme(
    primary = ShadDarkPrimary,
    onPrimary = ShadDarkPrimaryForeground,
    primaryContainer = ShadDarkMuted,
    onPrimaryContainer = ShadDarkSecondaryForeground,
    secondary = ShadDarkSecondary,
    onSecondary = ShadDarkSecondaryForeground,
    secondaryContainer = ShadDarkBorder,
    onSecondaryContainer = ShadDarkSecondaryForeground,
    tertiary = Color(0xFFFB923C),
    onTertiary = Color(0xFF1C1917),
    tertiaryContainer = ShadOrangeDarkContainer,
    onTertiaryContainer = ShadOnOrangeDarkContainer,
    error = ShadDarkDestructive,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFCA5A5),
    surface = ShadDarkCard,
    onSurface = ShadDarkForeground,
    surfaceVariant = ShadDarkMuted,
    onSurfaceVariant = ShadDarkMutedForeground,
    outline = ShadDarkBorder,
    outlineVariant = ShadDarkBorder,
    background = ShadDarkBackground,
    onBackground = ShadDarkForeground
)

val HackerFitTypography = Typography(
    displayLarge = Typography().displayLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = Typography().displayMedium.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    headlineLarge = Typography().headlineLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = Typography().headlineMedium.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleLarge = Typography().titleLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = Typography().titleMedium.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = Typography().labelLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = Typography().labelSmall.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun HackerFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HackerFitTypography,
        content = content
    )
}
