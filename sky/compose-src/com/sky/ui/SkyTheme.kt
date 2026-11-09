package com.sky.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import top.yukonga.miuix.kmp.theme.Colors

@Immutable
data class SkyColors(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val highlight: Color,
    val background: Color,
    val backgroundSecondary: Color,
    val card: Color,
    val overlay: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val onPrimary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val brandGradient: List<Color>,
    val auroraGradient: List<Color>,
    val dark: Boolean,
)

val LocalSkyColors = staticCompositionLocalOf { skyColors(dark = false) }

object SkyTheme {
    val colors: SkyColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSkyColors.current
}

fun skyColors(dark: Boolean): SkyColors = SkyColors(
    primary = if (dark) Color(0xFF60A5FA) else Color(0xFF3B82F6),
    secondary = if (dark) Color(0xFF3B82F6) else Color(0xFF60A5FA),
    accent = Color(0xFFA78BFA),
    highlight = Color(0xFF22D3EE),
    background = if (dark) Color(0xFF0B1220) else Color(0xFFFFFFFF),
    backgroundSecondary = if (dark) Color(0xFF111827) else Color(0xFFF8FAFC),
    card = if (dark) Color(0xFF1F2937) else Color(0xFFFFFFFF),
    overlay = if (dark) Color(0xFF374151) else Color(0xFFF8FAFC),
    text = if (dark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
    textSecondary = if (dark) Color(0xFFCBD5E1) else Color(0xFF64748B),
    textMuted = Color(0xFF94A3B8),
    border = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0),
    onPrimary = Color.Black,
    success = Color(0xFF10B981),
    warning = Color(0xFFF59E0B),
    error = Color(0xFFEF4444),
    info = Color(0xFF3B82F6),
    brandGradient = if (dark) {
        listOf(Color(0xFF0B1220), Color(0xFF172554), Color(0xFF3B82F6))
    } else {
        listOf(Color(0xFF3B82F6), Color(0xFF60A5FA), Color(0xFFA78BFA))
    },
    auroraGradient = listOf(Color(0xFF22D3EE), Color(0xFF60A5FA), Color(0xFFA78BFA)),
    dark = dark,
)

fun skyMiuixColors(tokens: SkyColors): Colors {
    val disabledPrimary = lerp(tokens.card, tokens.primary, 0.3f)
    val primaryTint = lerp(tokens.card, tokens.primary, 0.12f)
    val errorTint = lerp(tokens.card, tokens.error, 0.12f)

    return Colors(
        primary = tokens.primary,
        onPrimary = tokens.onPrimary,
        primaryVariant = tokens.primary,
        onPrimaryVariant = tokens.onPrimary,
        error = tokens.error,
        onError = Color.Black,
        errorContainer = errorTint,
        onErrorContainer = tokens.text,
        disabledPrimary = disabledPrimary,
        disabledOnPrimary = tokens.textMuted,
        disabledPrimaryButton = disabledPrimary,
        disabledOnPrimaryButton = tokens.textMuted,
        disabledPrimarySlider = disabledPrimary,
        primaryContainer = tokens.primary,
        onPrimaryContainer = tokens.onPrimary,
        // Miuix secondary is the unchecked switch track, not the secondary brand color.
        secondary = tokens.border,
        onSecondary = tokens.textSecondary,
        secondaryVariant = tokens.overlay,
        onSecondaryVariant = tokens.text,
        disabledSecondary = tokens.backgroundSecondary,
        disabledOnSecondary = tokens.textMuted,
        disabledSecondaryVariant = tokens.backgroundSecondary,
        disabledOnSecondaryVariant = tokens.textMuted,
        secondaryContainer = tokens.overlay,
        onSecondaryContainer = tokens.textSecondary,
        secondaryContainerVariant = tokens.backgroundSecondary,
        onSecondaryContainerVariant = tokens.textSecondary,
        tertiaryContainer = primaryTint,
        onTertiaryContainer = tokens.text,
        tertiaryContainerVariant = primaryTint,
        background = tokens.background,
        onBackground = tokens.text,
        onBackgroundVariant = tokens.textSecondary,
        surface = tokens.backgroundSecondary,
        onSurface = tokens.text,
        surfaceVariant = tokens.card,
        onSurfaceSecondary = tokens.textSecondary,
        onSurfaceVariantSummary = tokens.textSecondary,
        onSurfaceVariantActions = tokens.textMuted,
        disabledOnSurface = tokens.textMuted,
        surfaceContainer = tokens.card,
        onSurfaceContainer = tokens.text,
        onSurfaceContainerVariant = tokens.textSecondary,
        surfaceContainerHigh = tokens.overlay,
        onSurfaceContainerHigh = tokens.textSecondary,
        surfaceContainerHighest = tokens.overlay,
        onSurfaceContainerHighest = tokens.text,
        outline = tokens.border,
        dividerLine = tokens.border,
        windowDimming = Color.Black.copy(alpha = if (tokens.dark) 0.6f else 0.3f),
        sliderKeyPoint = tokens.textMuted.copy(alpha = 0.3f),
        sliderKeyPointForeground = tokens.secondary,
        sliderBackground = tokens.border,
    )
}
