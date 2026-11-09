// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package com.sky.ui.aboutEffect

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.sky.ui.SkyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import kotlin.math.floor

@Composable
fun BgEffectBackground(
    dynamicBackground: Boolean,
    modifier: Modifier = Modifier,
    bgModifier: Modifier = Modifier,
    isFullSize: Boolean = false,
    effectBackground: Boolean = true,
    isOs3Effect: Boolean = true,
    alpha: () -> Float = { 1f },
    content: @Composable (BoxScope.() -> Unit),
) {
    if (!remember { isRuntimeShaderSupported() }) {
        Box(modifier = modifier, content = content)
        return
    }
    Box(modifier = modifier) {
        val tokens = SkyTheme.colors
        val preset = remember(tokens, isOs3Effect) { BgEffectConfig.get(tokens.dark, isOs3Effect, tokens) }
        val painter = remember(isOs3Effect) { BgEffectPainter(isOs3Effect) }
        val colorStage = remember { Animatable(0f) }

        LaunchedEffect(dynamicBackground, preset) {
            if (!dynamicBackground) return@LaunchedEffect
            var targetStage = floor(colorStage.value) + 1f
            while (isActive) {
                delay((preset.colorInterpPeriod * 500).toLong())
                colorStage.animateTo(targetStage, animationSpec = spring(dampingRatio = 0.9f, stiffness = 35f))
                targetStage += 1f
            }
        }
        Spacer(
            modifier = Modifier.fillMaxSize().then(bgModifier).bgEffectDraw(
                painter = painter,
                preset = preset,
                deviceType = DeviceType.PHONE,
                isDarkTheme = tokens.dark,
                surface = tokens.background,
                effectBackground = effectBackground,
                isFullSize = isFullSize,
                playing = dynamicBackground,
                colorStage = { colorStage.value },
                alpha = alpha,
            ),
        )
        content()
    }
}
