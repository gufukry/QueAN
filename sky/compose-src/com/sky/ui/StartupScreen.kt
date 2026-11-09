package com.sky.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun StartupScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val finished by rememberUpdatedState(onFinished)
    val primary = MiuixTheme.colorScheme.primary
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1600, easing = FastOutSlowInEasing))
        delay(120)
        finished()
    }
    val value = progress.value
    Box(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface).safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(280.dp)) {
            val radius = size.minDimension * (0.22f + value * 0.18f)
            drawCircle(Brush.radialGradient(listOf(primary.copy(alpha = 0.12f * value), primary.copy(alpha = 0f))), radius * 1.4f)
            drawCircle(primary.copy(alpha = 0.24f * value), radius, style = Stroke(1.5.dp.toPx()))
            drawArc(primary.copy(alpha = value), -100f + value * 240f, 110f, false, topLeft = Offset(center.x - radius, center.y - radius), size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2), style = Stroke(3.dp.toPx()))
            val angle = value * Math.PI.toFloat() * 2f
            drawCircle(primary.copy(alpha = value), 4.dp.toPx(), Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius))
        }
        Text("sky", fontSize = 64.sp, fontWeight = FontWeight.Light, color = primary,
            modifier = Modifier.graphicsLayer { alpha = (value * 3).coerceAtMost(1f); scaleX = 0.85f + value * 0.15f; scaleY = scaleX; translationY = (1 - value) * 18.dp.toPx() })
    }
}
