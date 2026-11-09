package com.sky.ui

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference

@androidx.compose.runtime.Immutable
data class DebugOptions(
    val squircle: Boolean = true,
    val haptic: Boolean = true,
    val userScroll: Boolean = true,
    val topBar: Boolean = true,
    val smallTopBar: Boolean = false,
    val navigation: Boolean = true,
    val badge: Boolean = false,
    val navigationMode: Int = 0,
    val toolbar: Boolean = false,
    val fab: Boolean = false,
    val transition: Int = 0,
    val cornerClip: Boolean = true,
    val dim: Boolean = true,
    val blockInput: Boolean = true,
)

@Composable
internal fun DebugScreen(state: SkyState) {
    val options = state.debug
    Panel("显示与性能", preferenceGroup = true) {
        OverlayDropdownPreference(items = listOf("跟随系统", "浅色", "深色"), selectedIndex = listOf("system", "light", "dark").indexOf(state.themeMode).coerceAtLeast(0), title = "颜色模式", onSelectedIndexChange = { state.selectTheme(listOf("system", "light", "dark")[it]) })
        Toggle("启用超椭圆形状", "使用 Miuix 连续曲率圆角，关闭后使用普通圆角", options.squircle) { state.updateDebug(options.copy(squircle = it)) }
        Toggle("启用模糊效果", "为顶栏和悬浮底栏采样背景", state.blur) { state.setOption("blur", it) }
        Toggle("滚动末端触感", "快速滚动到边界时提供触感反馈", options.haptic) { state.updateDebug(options.copy(haptic = it)) }
    }
    Panel("页面与顶栏", preferenceGroup = true) {
        Toggle("允许页面横向滑动", "在主页、驱动、设置间横向切换", options.userScroll) { state.updateDebug(options.copy(userScroll = it)) }
        OverlayDropdownPreference(items = listOf("原生", "跨轴拦截", "点按停止惯性"), selectedIndex = listOf("native", "cross", "halt").indexOf(state.pageGesture).coerceAtLeast(0), title = "页面手势模式", onSelectedIndexChange = { state.selectPageGesture(listOf("native", "cross", "halt")[it]) })
        Toggle("显示顶部栏", "调试页始终保留返回与恢复入口", options.topBar) { state.updateDebug(options.copy(topBar = it)) }
        OverlayDropdownPreference(items = listOf("大标题随滚动收起", "紧凑标题"), selectedIndex = if (options.smallTopBar) 1 else 0, title = "顶部栏样式", onSelectedIndexChange = { state.updateDebug(options.copy(smallTopBar = it == 1)) })
    }
    Panel("导航与悬浮控件", preferenceGroup = true) {
        Toggle("显示导航栏", "调试页仍可通过顶部返回设置", options.navigation) { state.updateDebug(options.copy(navigation = it)) }
        Toggle("显示导航角标演示", "设置图标显示“演示”角标，不代表真实通知", options.badge) { state.updateDebug(options.copy(badge = it)) }
        OverlayDropdownPreference(items = listOf("图标与文字", "仅图标", "选中时显示文字"), selectedIndex = options.navigationMode.coerceIn(0, 2), title = "导航栏显示模式", onSelectedIndexChange = { state.updateDebug(options.copy(navigationMode = it)) })
        Toggle("使用悬浮导航栏", "使用 KSU 悬浮底栏组件", state.floatingBar) { state.setOption("floating_bar", it) }
        Toggle("显示悬浮工具栏", "工具栏提供返回主页与恢复默认调试设置", options.toolbar) { state.updateDebug(options.copy(toolbar = it)) }
        Toggle("显示悬浮操作按钮", "点击进入调试页", options.fab) { state.updateDebug(options.copy(fab = it)) }
    }
    Panel("转场与返回", preferenceGroup = true) {
        OverlayDropdownPreference(items = listOf("平滑滑动", "淡入淡出", "即时切换"), selectedIndex = options.transition.coerceIn(0, 2), title = "转场样式", onSelectedIndexChange = { state.updateDebug(options.copy(transition = it)) })
        Toggle("启用转场圆角裁剪", "转场中的页面使用圆角边界", options.cornerClip) { state.updateDebug(options.copy(cornerClip = it)) }
        Toggle("启用转场暗化", "离场页面在转场期间渐暗", options.dim) { state.updateDebug(options.copy(dim = it)) }
        Toggle("转场期间阻止输入", "阻止非目标页面接收触摸", options.blockInput) { state.updateDebug(options.copy(blockInput = it)) }
        Toggle("启用侧滑返回", "从左侧边缘返回上一页", state.swipeBack) { state.setOption("swipe_back", it) }
    }
    Panel("恢复") { Action("恢复默认调试设置", state::resetDebug) }
}
