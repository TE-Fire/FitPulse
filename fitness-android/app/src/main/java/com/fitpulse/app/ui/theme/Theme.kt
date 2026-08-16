package com.fitpulse.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = Tertiary,
    secondary = Secondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = Tertiary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Neutral60,
    outline = Divider,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryContainer,
    primaryContainer = Primary,
    secondary = SecondaryContainer,
    secondaryContainer = Secondary,
    background = Color(0xFF121212),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFF5F5F5),
)

@Composable
fun FitPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitPulseTypography,
        content = content
    )
}
