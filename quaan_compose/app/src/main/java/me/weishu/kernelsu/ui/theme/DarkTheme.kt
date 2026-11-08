package me.weishu.kernelsu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSkyDarkTheme = staticCompositionLocalOf<Boolean?> { null }

@Composable
fun isInDarkTheme(): Boolean = LocalSkyDarkTheme.current ?: isSystemInDarkTheme()
