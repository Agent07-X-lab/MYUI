package com.jndapp.minimal.widgets.ui.theme

import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MyUIDarkColorScheme = darkColorScheme(
    primary = MyUIPrimary,
    onPrimary = MyUIBackground,
    primaryContainer = MyUISurfaceVariant,
    onPrimaryContainer = MyUIPrimary,
    secondary = MyUIPrimary,
    onSecondary = MyUIBackground,
    background = MyUIBackground,
    surface = MyUISurface,
    surfaceVariant = MyUISurfaceVariant,
    onBackground = MyUITextPrimary,
    onSurface = MyUITextPrimary,
    onSurfaceVariant = MyUITextSecondary,
    outline = MyUIBorder,
    outlineVariant = MyUIBorder
)

private val MyUILightColorScheme = lightColorScheme(
    primary = MyUIPrimary,
    onPrimary = MyUIBackground,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = Color(0xFF6B5300),
    secondary = AccentBlue,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = Color(0xFFD8D8DF)
)

@Composable
fun MyUITheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MyUIDarkColorScheme else MyUILightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MinimalWidgetsTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = MyUITheme(darkTheme = darkTheme, content = content)
