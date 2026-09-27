package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MyraColorScheme = darkColorScheme(
    primary = MyraNeonGreen,
    onPrimary = Color(0xFF021B0C),
    primaryContainer = Color(0xFF0F3820),
    onPrimaryContainer = Color(0xFFA1F7C5),
    secondary = MyraCyberCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = MyraMatrixGreen,
    onTertiary = Color(0xFF002206),
    background = MyraDeepBlack,
    onBackground = MyraTextPrimary,
    surface = MyraCardBg,
    onSurface = MyraTextPrimary,
    surfaceVariant = MyraCardBgElevated,
    onSurfaceVariant = MyraTextSecondary,
    outline = MyraBorder,
    outlineVariant = MyraBorderGlow,
    error = MyraAlertRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MyraColorScheme,
        typography = Typography,
        content = content
    )
}
