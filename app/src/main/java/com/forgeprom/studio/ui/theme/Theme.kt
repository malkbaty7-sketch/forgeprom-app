package com.forgeprom.studio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.forgeprom.studio.core.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = PurplePrimary,
    secondary = RoyalBlue,
    tertiary = Gold
)

private val DarkColors = darkColorScheme(
    primary = PurpleLight,
    secondary = RoyalBlue,
    tertiary = Gold
)

@Composable
fun ForgepromTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
