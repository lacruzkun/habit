package com.focusflow.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

object Palette {
    val Purple = Color(0xFF6C4CF1)
    val PurpleDeep = Color(0xFF4A32C4)
    val Indigo = Color(0xFF2E2459)
    val Lavender = Color(0xFFD8CCFA)
    val LavenderSoft = Color(0xFFF1EBFF)
    val Peach = Color(0xFFFFB877)
    val PeachSoft = Color(0xFFFFE9D8)
    val Mint = Color(0xFF3FCB92)
    val MintSoft = Color(0xFFDDF7EB)
    val Pink = Color(0xFFFF7A9E)
    val PinkSoft = Color(0xFFFFE2EB)
    val Sky = Color(0xFF67B7F7)
    val SkySoft = Color(0xFFE2F1FF)
    val Cream = Color(0xFFFBF4FD)
    val White = Color(0xFFFFFFFF)
    val Danger = Color(0xFFE5484D)
    val DarkBackground = Color(0xFF151022)
    val DarkSurface = Color(0xFF211933)
    val DarkSurfaceVariant = Color(0xFF2D2443)
}

@Immutable
data class FocusColors(
    val lavender: Color,
    val peach: Color,
    val mint: Color,
    val pink: Color,
    val sky: Color,
    val ink: Color,
    val muted: Color,
    val grid: Color,
    val card: Color,
    val background: Color,
    val backgroundGradient: List<Color>,
    val success: Color,
    val danger: Color,
    val navy: Color = Color(0xFF2E2459),
)

val LightFocusColors = FocusColors(
    lavender = Palette.LavenderSoft,
    peach = Palette.PeachSoft,
    mint = Palette.MintSoft,
    pink = Palette.PinkSoft,
    sky = Palette.SkySoft,
    ink = Palette.Indigo,
    muted = Color(0xFF7D7398),
    grid = Color(0xFFE9E1F7),
    card = Color.White,
    background = Palette.Cream,
    backgroundGradient = listOf(Color(0xFFEFE7FF), Color(0xFFF8F3FF), Color(0xFFF3ECFF)),
    success = Color(0xFF1E9E6A),
    danger = Palette.Danger,
)

val DarkFocusColors = FocusColors(
    lavender = Color(0xFF322653),
    peach = Color(0xFF3C2C24),
    mint = Color(0xFF1D352C),
    pink = Color(0xFF3A2430),
    sky = Color(0xFF1E3044),
    ink = Color(0xFFF4EEFF),
    muted = Color(0xFFB3A8CC),
    grid = Color(0xFF3A3053),
    card = Palette.DarkSurface,
    background = Palette.DarkBackground,
    backgroundGradient = listOf(Color(0xFF191226), Color(0xFF0F0B18)),
    success = Color(0xFF6FE3B4),
    danger = Color(0xFFFF8A8E),
)
