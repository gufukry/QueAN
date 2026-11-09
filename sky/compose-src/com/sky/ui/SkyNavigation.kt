package com.sky.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Settings
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.weishu.kernelsu.ui.component.FloatingBottomBar
import me.weishu.kernelsu.ui.component.FloatingBottomBarItem
import top.yukonga.miuix.kmp.blur.Backdrop

@Composable
fun SkyNavigation(selected: Int, backdrop: Backdrop, floating: Boolean = true, glass: Boolean = true, options: DebugOptions = DebugOptions(), onSelect: (Int) -> Unit) {
    val labels = listOf("主页", "驱动", "设置")
    val icons = listOf(Icons.Rounded.Cottage, Icons.Rounded.Extension, Icons.Rounded.Settings)
    if (!floating) {
        val background = SkyTheme.colors.backgroundSecondary
        Box(if (glass) Modifier.textureBlur(backdrop = backdrop, shape = RectangleShape, blurRadius = 25f, colors = BlurColors(blendColors = listOf(BlendColorEntry(background.copy(alpha = 0.72f))))) else Modifier) {
        top.yukonga.miuix.kmp.basic.NavigationBar(color = if (glass) Color.Transparent else background, defaultWindowInsetsPadding = false, mode = top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode.entries[options.navigationMode.coerceIn(0, 2)]) {
            labels.forEachIndexed { index, label ->
                MiuixNavigationBarItem(icon = icons[index], label = label, selected = selected == index, onClick = { onSelect(index) }, modifier = Modifier.weight(1f), badge = if (options.badge && index == 2) { { top.yukonga.miuix.kmp.basic.Badge { Text("演示") } } } else null)
            }
        }
        }
    } else {
        FloatingBottomBar(selectedIndex = selected, onSelected = onSelect, backdrop = backdrop, tabsCount = 3, isBlurEnabled = glass) { activate ->
            labels.forEachIndexed { index, label ->
                FloatingBottomBarItem(selected = selected == index, onClick = { activate(index) }, modifier = Modifier.defaultMinSize(minWidth = 76.dp)) {
                    top.yukonga.miuix.kmp.basic.BadgedBox(badge = { if (options.badge && index == 2) top.yukonga.miuix.kmp.basic.Badge { Text("演示") } }) {
                        top.yukonga.miuix.kmp.basic.Icon(imageVector = icons[index], contentDescription = label)
                    }
                    if (options.navigationMode == 0 || (options.navigationMode == 2 && selected == index)) top.yukonga.miuix.kmp.basic.Text(text = label, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
