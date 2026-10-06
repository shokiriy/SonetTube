package com.shokirjon.sonettube.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Coral,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Slate,
    background = Mist,
    surface = androidx.compose.ui.graphics.Color.White,
)

private val DarkColors = darkColorScheme(
    primary = CoralDark,
    onPrimary = Ink,
    secondary = CoralDark,
    background = Ink,
    surface = Slate,
)

@Composable
fun SonetTubeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
