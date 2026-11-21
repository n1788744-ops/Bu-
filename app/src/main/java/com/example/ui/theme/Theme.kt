package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = ElectricSky,
    onPrimary = ObsidianDeep,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = TextPrimaryDark,
    secondary = TelegramBlue,
    onSecondary = PureWhite,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = SafeEmerald,
    onTertiary = ObsidianDeep,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    error = DangerCoral,
    onError = PureWhite,
    errorContainer = Color(0xFF45161B),
    onErrorContainer = DangerCoralSoft,
)

private val LightColorScheme = lightColorScheme(
    primary = Obsidian,
    onPrimary = PureWhite,
    primaryContainer = SoftMist,
    onPrimaryContainer = TextPrimaryLight,
    secondary = ElectricSkyDark,
    onSecondary = PureWhite,
    secondaryContainer = TelegramBlueSoft,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = SafeEmerald,
    onTertiary = PureWhite,
    background = SoftMist,
    onBackground = TextPrimaryLight,
    surface = PureWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = DangerCoral,
    onError = PureWhite,
    errorContainer = DangerCoralSoft,
    onErrorContainer = Color(0xFF7F1D1D),
)

private fun Color(hex: Long): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(hex)

val KBrowserShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun KBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KBrowserTypography,
        shapes = KBrowserShapes,
        content = content,
    )
}
