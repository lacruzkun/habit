package com.focusflow.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.focusflow.app.domain.model.ThemeMode

val LocalFocusColors = staticCompositionLocalOf { LightFocusColors }

private val LightScheme = lightColorScheme(
    primary = Palette.Purple,
    onPrimary = Color.White,
    primaryContainer = Palette.LavenderSoft,
    onPrimaryContainer = Palette.Indigo,
    secondary = Color(0xFFE68A4D),
    onSecondary = Color.White,
    secondaryContainer = Palette.PeachSoft,
    onSecondaryContainer = Color(0xFF5A3518),
    tertiary = Palette.Mint,
    onTertiary = Color.White,
    tertiaryContainer = Palette.MintSoft,
    onTertiaryContainer = Color(0xFF12523A),
    background = Palette.Cream,
    onBackground = Palette.Indigo,
    surface = Color.White,
    onSurface = Palette.Indigo,
    surfaceVariant = Color(0xFFF1EBFB),
    onSurfaceVariant = Color(0xFF6F658A),
    outline = Color(0xFFD8CDEF),
    outlineVariant = Color(0xFFEAE3F7),
    error = Palette.Danger,
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E4),
    onErrorContainer = Color(0xFF7A1D20),
    scrim = Color(0x66000000),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFC8B6FF),
    onPrimary = Color(0xFF2B1B62),
    primaryContainer = Color(0xFF48368C),
    onPrimaryContainer = Color(0xFFE9E0FF),
    secondary = Color(0xFFFFC79C),
    onSecondary = Color(0xFF4A2A12),
    secondaryContainer = Color(0xFF5C3A22),
    onSecondaryContainer = Color(0xFFFFE1CA),
    tertiary = Color(0xFF7BE3B4),
    onTertiary = Color(0xFF083B28),
    tertiaryContainer = Color(0xFF1D4C38),
    onTertiaryContainer = Color(0xFFC7F7DF),
    background = Palette.DarkBackground,
    onBackground = Color(0xFFF4EEFF),
    surface = Palette.DarkSurface,
    onSurface = Color(0xFFF4EEFF),
    surfaceVariant = Palette.DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC6BADF),
    outline = Color(0xFF574B73),
    outlineVariant = Color(0xFF3C3253),
    error = Color(0xFFFF8A8E),
    onError = Color(0xFF4A0B0D),
    errorContainer = Color(0xFF5E2224),
    onErrorContainer = Color(0xFFFFDADB),
    scrim = Color(0x99000000),
)

@Composable
fun FocusFlowTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val scheme = if (dark) DarkScheme else LightScheme
    val colors = if (dark) DarkFocusColors else LightFocusColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalFocusColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = FocusFlowTypography,
            content = content,
        )
    }
}
