package com.wickwirez.linkwarden.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val VoidBlack = Color(0xFF0A0E14)
val PanelDark = Color(0xFF121822)
val PanelElevated = Color(0xFF1B2430)
val BorderSubtle = Color(0xFF2A3542)
val TextPrimary = Color(0xFFE7EDF3)
val TextMuted = Color(0xFF7A8699)
val SignalSafe = Color(0xFF00E5A0)
val SignalCaution = Color(0xFFFFB020)
val SignalDanger = Color(0xFFFF3B5C)
val AccentCyan = Color(0xFF3FD0FF)

val MonoFamily = FontFamily.Monospace
val SansFamily = FontFamily.SansSerif

private val LinkWardenColorScheme = darkColorScheme(
    background = VoidBlack,
    surface = PanelDark,
    surfaceVariant = PanelElevated,
    primary = AccentCyan,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderSubtle,
    error = SignalDanger
)

private val LinkWardenTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 3.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SansFamily,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SansFamily,
        fontSize = 12.sp,
        color = TextMuted
    )
)

@Composable
fun LinkWardenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LinkWardenColorScheme,
        typography = LinkWardenTypography,
        content = content
    )
}
