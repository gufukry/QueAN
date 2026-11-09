// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package com.sky.ui.aboutEffect

import androidx.compose.ui.graphics.Color
import com.sky.ui.SkyColors

internal object BgEffectConfig {
    internal class Config(
        val points: FloatArray,
        val colors1: FloatArray,
        val colors2: FloatArray,
        val colors3: FloatArray,
        val colorInterpPeriod: Float,
        val lightOffset: Float,
        val saturateOffset: Float,
        val pointOffset: Float,
    )

    private val phonePoints = floatArrayOf(
        0.8f, 0.2f, 1.0f,
        0.8f, 0.9f, 1.0f,
        0.2f, 0.9f, 1.0f,
        0.2f, 0.2f, 1.0f,
    )

    internal fun get(isDark: Boolean, isOs3: Boolean, tokens: SkyColors): Config {
        val colors = tokenColors(tokens, isDark)
        return if (isOs3) {
            Config(phonePoints, colors[0], colors[1], colors[2], if (isDark) 8f else 5f, if (isDark) 0f else 0.1f, 0.2f, if (isDark) 0.4f else 0.2f)
        } else {
            Config(phonePoints, colors[0], colors[0], colors[0], 100f, if (isDark) -0.1f else 0.1f, 0.2f, 0.1f)
        }
    }

    private fun tokenColors(tokens: SkyColors, dark: Boolean): Array<FloatArray> {
        val palette = if (dark) {
            listOf(tokens.primary, tokens.accent, tokens.highlight, tokens.secondary)
        } else {
            listOf(tokens.highlight, tokens.primary, tokens.accent, tokens.secondary)
        }
        fun rgba(color: Color): FloatArray = floatArrayOf(color.red, color.green, color.blue, color.alpha)
        fun frame(offset: Int): FloatArray = FloatArray(16) { index -> rgba(palette[(index / 4 + offset) % palette.size])[index % 4] }
        return arrayOf(frame(0), frame(1), frame(2))
    }
}
