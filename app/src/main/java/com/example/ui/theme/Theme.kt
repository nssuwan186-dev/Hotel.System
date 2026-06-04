package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HotelGold,
    secondary = SoftGold,
    tertiary = ActiveBlue,
    background = DeepSlateBg,
    surface = SoftCardBg,
    onPrimary = DeepSlateBg,
    onSecondary = DeepSlateBg,
    onBackground = SoftGold,
    onSurface = SoftGold
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLightNavy,
    secondary = HotelGold,
    tertiary = ActiveBlue,
    background = HighContrastLightBg,
    surface = SoftLightCard,
    onPrimary = SoftLightCard,
    onSecondary = PrimaryLightNavy,
    onBackground = PrimaryLightNavy,
    onSurface = PrimaryLightNavy
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,  // Enforce premium luxury dark style by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
