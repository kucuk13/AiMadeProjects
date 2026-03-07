package com.example.countdownapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

/**
 * Simple theme wrapper for the countdown app.  This theme delegates to
 * Compose's [MaterialTheme] using default light and dark color palettes.
 * Colors and typography are defined in XML resource files; this wrapper
 * merely switches between palettes based on the system's dark mode.
 */
@Composable
fun CountdownAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColors()
    } else {
        lightColors()
    }
    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}