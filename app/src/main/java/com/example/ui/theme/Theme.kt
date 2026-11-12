package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FrescutColorScheme = lightColorScheme(
    primary = FrescutRedBright,
    onPrimary = FrescutWhite,
    primaryContainer = FrescutRedLightBg,
    onPrimaryContainer = FrescutRedDark,
    secondary = FrescutGoldDark,
    onSecondary = FrescutWhite,
    secondaryContainer = FrescutOrangeBg,
    onSecondaryContainer = FrescutTextPrimary,
    tertiary = FrescutGreenDiscount,
    onTertiary = FrescutWhite,
    background = FrescutWhite,
    onBackground = FrescutTextPrimary,
    surface = FrescutWhite,
    onSurface = FrescutTextPrimary,
    surfaceVariant = FrescutSurface,
    onSurfaceVariant = FrescutTextSecondary,
    outline = FrescutBorderLight
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FrescutColorScheme,
        typography = Typography,
        content = content
    )
}
