package com.arise.habitquest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AriseColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val elevated: Color,
    val primary: Color,
    val primaryLight: Color,
    val primaryGlow: Color,
    val secondary: Color,
    val gold: Color,
    val goldGlow: Color,
    val crimson: Color,
    val crimsonGlow: Color,
    val emerald: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDim: Color,
    val textSystem: Color,
    val border: Color,
    val borderAccent: Color
)

val LocalAriseColors = staticCompositionLocalOf {
    AriseColors(
        background = BackgroundDeep,
        surface = BackgroundSurface,
        card = BackgroundCard,
        elevated = BackgroundElevated,
        primary = PurpleCore,
        primaryLight = PurpleLight,
        primaryGlow = PurpleGlow,
        secondary = BlueCore,
        gold = GoldCore,
        goldGlow = GoldGlow,
        crimson = CrimsonCore,
        crimsonGlow = CrimsonGlow,
        emerald = EmeraldCore,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textDim = TextDim,
        textSystem = TextSystem,
        border = BorderDefault,
        borderAccent = BorderAccent
    )
}

private val AriseDarkColorScheme = darkColorScheme(
    primary = PurpleCore,
    onPrimary = TextPrimary,
    primaryContainer = PurpleDim,
    onPrimaryContainer = PurpleLight,
    secondary = BlueCore,
    onSecondary = TextPrimary,
    secondaryContainer = BlueDim,
    onSecondaryContainer = BlueLight,
    tertiary = GoldCore,
    onTertiary = BackgroundDeep,
    tertiaryContainer = GoldDim,
    onTertiaryContainer = GoldLight,
    error = CrimsonCore,
    onError = TextPrimary,
    errorContainer = CrimsonDim,
    onErrorContainer = CrimsonLight,
    background = BackgroundDeep,
    onBackground = TextPrimary,
    surface = BackgroundSurface,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    outlineVariant = BorderAccent,
    scrim = Color(0xCC000000),
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundDeep,
    inversePrimary = PurpleDim,
    surfaceTint = PurpleGlow
)

val ariseColorDefaults = AriseColors(
    background = BackgroundDeep,
    surface = BackgroundSurface,
    card = BackgroundCard,
    elevated = BackgroundElevated,
    primary = PurpleCore,
    primaryLight = PurpleLight,
    primaryGlow = PurpleGlow,
    secondary = BlueCore,
    gold = GoldCore,
    goldGlow = GoldGlow,
    crimson = CrimsonCore,
    crimsonGlow = CrimsonGlow,
    emerald = EmeraldCore,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textDim = TextDim,
    textSystem = TextSystem,
    border = BorderDefault,
    borderAccent = BorderAccent
)

@Composable
fun AriseTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAriseColors provides ariseColorDefaults
    ) {
        MaterialTheme(
            colorScheme = AriseDarkColorScheme,
            typography = AriseTypography,
            content = content
        )
    }
}

// Convenience accessor — use AriseTheme.colors.xxx in composables
object AriseTheme {
    val colors: AriseColors
        @Composable
        get() = LocalAriseColors.current
    val typography
        @Composable
        get() = MaterialTheme.typography
}
