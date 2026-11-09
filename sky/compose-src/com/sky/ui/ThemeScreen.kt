package com.sky.ui

import androidx.compose.runtime.*

@Composable
internal fun ThemeScreen(state: SkyState) {
    top.yukonga.miuix.kmp.basic.TabRow(colors = top.yukonga.miuix.kmp.basic.TabRowDefaults.tabRowColors(backgroundColor = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface), tabs = listOf("跟随系统", "浅色", "深色"), selectedTabIndex = listOf("system", "light", "dark").indexOf(state.themeMode).coerceAtLeast(0), onTabSelected = { state.selectTheme(listOf("system", "light", "dark")[it]) })
    Panel("页面与导航", preferenceGroup = true) {
        Toggle("允许界面横向滑动", "在主页、驱动和设置之间滑动", state.debug.userScroll) { state.updateDebug(state.debug.copy(userScroll = it)) }
        top.yukonga.miuix.kmp.preference.OverlayDropdownPreference(items = listOf("图标与文字", "仅图标", "选中时显示文字"), selectedIndex = state.debug.navigationMode.coerceIn(0, 2), title = "导航栏显示模式", onSelectedIndexChange = { state.updateDebug(state.debug.copy(navigationMode = it)) })
    }
    Panel("导航效果", preferenceGroup = true) {
        Toggle("模糊", "启用顶栏和悬浮底栏的模糊效果", state.blur) { state.setOption("blur", it) }
        Toggle("悬浮底栏", "使用悬浮胶囊导航栏", state.floatingBar) { state.setOption("floating_bar", it) }
        Toggle("液态玻璃", "启用悬浮底栏的液态玻璃效果，需要同时启用模糊。", state.liquidGlass) { state.setOption("liquid_glass", it) }
    }
}

