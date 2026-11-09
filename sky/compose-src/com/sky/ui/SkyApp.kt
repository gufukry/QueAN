package com.sky.ui

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.input.pointer.PointerEventPass
import kotlinx.coroutines.flow.first
import top.yukonga.miuix.kmp.utils.PagerInterceptionMode
import top.yukonga.miuix.kmp.utils.PagerGestureNestedScrollConnection
import top.yukonga.miuix.kmp.utils.pagerGestureOverride
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import com.sky.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.weishu.kernelsu.ui.theme.LocalSkyDarkTheme
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SkyApp(state: SkyState, version: String, onChooseDriver: () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (state.themeMode) { "system" -> systemDark; "light" -> false; else -> true }
    Crossfade(targetState = dark, animationSpec = tween(300, easing = FastOutSlowInEasing), label = "appearance") { targetDark ->
        SkyAppearance(state, version, onChooseDriver, targetDark)
    }
}

@Composable
private fun SkyAppearance(state: SkyState, version: String, onChooseDriver: () -> Unit, dark: Boolean) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val tokens = remember(dark) { skyColors(dark) }
    val colors = remember(tokens) { skyMiuixColors(tokens) }
    CompositionLocalProvider(
        LocalDensity provides Density(density.density * state.scale / 100f, density.fontScale),
        LocalSkyDarkTheme provides dark,
        LocalSkyColors provides tokens,
        top.yukonga.miuix.kmp.squircle.LocalSquircleEnabled provides state.debug.squircle,
    ) {
        MiuixTheme(colors = colors) {
            val background = tokens.background
            SideEffect {
                (context as? ComponentActivity)?.let { activity ->
                    activity.enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark },
                        navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark })
                    activity.window.isStatusBarContrastEnforced = false
                    activity.window.isNavigationBarContrastEnforced = false
                    activity.window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(background.toArgb()))
                }
            }
            top.yukonga.miuix.kmp.basic.Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), containerColor = background) {
                StartupContent(state, version, onChooseDriver)
            }
        }
    }
}

@Composable
private fun StartupContent(state: SkyState, version: String, onChooseDriver: () -> Unit) {
    Crossfade(targetState = state.startupComplete, animationSpec = tween(350), label = "startup") { complete ->
        if (complete) MainScreen(state, version, onChooseDriver)
        else StartupScreen(state::finishStartup)
    }
}

@OptIn(androidx.activity.ExperimentalActivityApi::class)
@Composable
private fun MainScreen(state: SkyState, version: String, onChooseDriver: () -> Unit) {
    val miuixScroll = key(state.page) { top.yukonga.miuix.kmp.basic.MiuixScrollBehavior() }
    val scrollModifier = Modifier.nestedScroll(miuixScroll.nestedScrollConnection)
    val surface = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop { drawRect(surface); drawContent() }
    val floating = state.floatingBar && state.debug.navigation
    val debug = state.debug
    val pager = rememberPagerState(initialPage = state.page.coerceIn(0, 2), pageCount = { 3 })
    val pagerMode = when (state.pageGesture) { "native" -> PagerInterceptionMode.Native; "halt" -> PagerInterceptionMode.TapToHalt; else -> PagerInterceptionMode.CrossAxisInterceptor }
    LaunchedEffect(state.page) {
        if (state.page < 3 && pager.currentPage != state.page) pager.animateScrollToPage(state.page)
    }
    LaunchedEffect(pager) {
        snapshotFlow { pager.isScrollInProgress }.collect { moving ->
            if (moving) {
                snapshotFlow { pager.isScrollInProgress }.first { !it }
                if (state.page < 3) state.page = pager.settledPage
            }
        }
    }
    val sceneTransition = updateTransition(targetState = if (state.page < 3) 0 else state.page, label = "scene")
    val subpage = state.page >= 3
    val goBack = { state.page = if (state.page >= 4) 2 else 0 }
    var backProgress by remember { mutableFloatStateOf(0f) }
    BackHandler(enabled = state.page != 0 && !state.predictiveBack, onBack = goBack)
    PredictiveBackHandler(enabled = state.page != 0 && state.predictiveBack) { events ->
        try { events.collect { backProgress = it.progress }; goBack() } finally { backProgress = 0f }
    }
    var distance by remember { mutableFloatStateOf(0f) }
    var edge by remember { mutableStateOf(false) }
    val gestureModifier = if (subpage && state.swipeBack) Modifier.pointerInput(state.page) {
        detectHorizontalDragGestures(onDragStart = { edge = it.x < 28.dp.toPx(); distance = 0f }, onDragEnd = {
            if (edge && state.swipeBack && state.page != 0 && distance > 64.dp.toPx()) goBack()
            distance = 0f
        }, onHorizontalDrag = { change, amount ->
            distance += amount
            if (edge) change.consume()
        })
    } else Modifier
    val density = LocalDensity.current
    var topBarHeight by remember { mutableIntStateOf(0) }
    val topPadding = with(density) { topBarHeight.toDp() }
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    Box(Modifier.fillMaxSize().background(surface).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)).then(scrollModifier).then(gestureModifier)) {
        Column(Modifier.fillMaxSize()) {
            val contentBottom = if (!subpage && debug.navigation) 104.dp else 0.dp
            sceneTransition.AnimatedContent(modifier = Modifier.weight(1f).layerBackdrop(backdrop).then(
                if (debug.cornerClip && sceneTransition.isRunning) Modifier.clip(RoundedCornerShape(24.dp)) else Modifier
            ).graphicsLayer { translationX = backProgress * size.width * 0.15f; alpha = 1f - backProgress * 0.25f }, transitionSpec = {
                when (debug.transition) {
                    2 -> EnterTransition.None togetherWith ExitTransition.None
                    1 -> fadeIn(tween(240)) togetherWith fadeOut(tween(180))
                    else -> {
                        val direction = if (targetState > initialState) 1 else -1
                        (fadeIn(tween(260)) + slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it / 8 * direction }) togetherWith
                            (fadeOut(tween(180)) + slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it / 8 * direction })
                    }
                }
            }) { scene ->
                Box(Modifier.fillMaxSize().then(if (debug.blockInput && sceneTransition.isRunning && scene != sceneTransition.targetState) Modifier.pointerInput(Unit) {
                    awaitPointerEventScope { while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() } }
                } else Modifier)) {
                    if (scene == 0) HorizontalPager(
                        state = pager,
                        modifier = Modifier.fillMaxSize().pagerGestureOverride(pagerState = pager, mode = pagerMode, enabled = debug.userScroll),
                        userScrollEnabled = debug.userScroll && pagerMode != PagerInterceptionMode.CrossAxisInterceptor,
                        pageNestedScrollConnection = if (pagerMode == PagerInterceptionMode.CrossAxisInterceptor) PagerGestureNestedScrollConnection else PagerDefaults.pageNestedScrollConnection(pager, Orientation.Horizontal),
                    ) { page ->
                        PageBody(state, page, version, onChooseDriver, topPadding, contentBottom)
                    } else PageBody(state, scene, version, onChooseDriver, topPadding, contentBottom)
                    if (debug.dim && sceneTransition.isRunning && scene != sceneTransition.targetState) Box(Modifier.fillMaxSize().background(SkyTheme.colors.text.copy(alpha = 0.12f)))
                }
            }
        }
        val showTop = debug.topBar || state.page == 6 || state.page == 7
        if (showTop) Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().onSizeChanged { topBarHeight = it.height }.then(
            if (state.blur) Modifier.textureBlur(
                backdrop = backdrop,
                shape = androidx.compose.ui.graphics.RectangleShape,
                blurRadius = 25f,
                colors = BlurColors(blendColors = listOf(BlendColorEntry(surface.copy(alpha = 0.72f)))),
            ) else Modifier.background(surface)
        ).statusBarsPadding()) {
            val title = listOf("主页", "驱动", "设置", "", "主题设置", "关于", "调试", "终端")[state.page]
            val back: @Composable () -> Unit = { if (subpage) top.yukonga.miuix.kmp.basic.IconButton(onClick = goBack) { top.yukonga.miuix.kmp.basic.Icon(Icons.AutoMirrored.Rounded.ArrowBack, "返回") } }
            if (debug.smallTopBar) top.yukonga.miuix.kmp.basic.SmallTopAppBar(title = title, defaultWindowInsetsPadding = false, color = Color.Transparent, navigationIcon = back)
            else top.yukonga.miuix.kmp.basic.TopAppBar(title = title, defaultWindowInsetsPadding = false, scrollBehavior = miuixScroll, color = Color.Transparent, navigationIcon = back)
        }
        LaunchedEffect(showTop) { if (!showTop) topBarHeight = statusBarHeight }
        if (!floating && !subpage && debug.navigation) Box(Modifier.align(Alignment.BottomCenter)) {
            SkyNavigation(state.page, backdrop, floating = false, glass = state.blur, options = debug) { state.page = it }
        }
        if (floating && !subpage) Box(Modifier.align(Alignment.BottomCenter).padding(horizontal = 28.dp, vertical = 8.dp)) {
            SkyNavigation(state.page, backdrop, glass = state.blur && state.liquidGlass, options = debug) { state.page = it }
        }
        if (debug.toolbar) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = if (floating && !subpage) 96.dp else 12.dp)) {
            top.yukonga.miuix.kmp.basic.FloatingToolbar {
                Row { top.yukonga.miuix.kmp.basic.TextButton(text = "主页", onClick = { state.page = 0 }); top.yukonga.miuix.kmp.basic.TextButton(text = "恢复默认", onClick = state::resetDebug) }
            }
        }
        if (debug.fab) top.yukonga.miuix.kmp.basic.FloatingActionButton(onClick = { state.page = 6 }, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = if (floating && !subpage) 100.dp else 16.dp)) {
            top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.BugReport, "调试", tint = SkyTheme.colors.onPrimary)
        }

    }
}

@Composable
private fun PageBody(state: SkyState, page: Int, version: String, onChooseDriver: () -> Unit, topPadding: androidx.compose.ui.unit.Dp, contentBottom: androidx.compose.ui.unit.Dp) {
    val haptic = if (state.debug.haptic) Modifier.scrollEndHaptic() else Modifier
    if (page == 5) {
        Box(Modifier.fillMaxSize().padding(top = topPadding)) { AboutScreen(version, state) }
        return
    }
    Column(Modifier.fillMaxSize().background(SkyTheme.colors.backgroundSecondary).then(haptic).verticalScroll(rememberScrollState()).padding(start = 12.dp, end = 12.dp, top = topPadding + 8.dp, bottom = contentBottom + 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (page) {
            0 -> HomeScreen(state, version)
            1 -> DriverScreen(state, onChooseDriver)
            2 -> SettingsScreen(state, version)
            4 -> ThemeScreen(state)
            6 -> DebugScreen(state)
            7 -> TerminalScreen(state)
        }
    }
}

@Composable
internal fun Panel(title: String, summary: String = "", preferenceGroup: Boolean = false, content: @Composable ColumnScope.() -> Unit = {}) {
    val inner: @Composable ColumnScope.() -> Unit = {
        if (preferenceGroup) {
            content()
        } else Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            if (summary.isNotEmpty()) Text(summary, fontSize = 13.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            content()
        }
    }
    top.yukonga.miuix.kmp.basic.Card(modifier = Modifier.fillMaxWidth(), content = inner)
}

@Composable
internal fun Action(label: String, onClick: () -> Unit) {
    top.yukonga.miuix.kmp.basic.Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(label) }
}

@Composable
internal fun Toggle(title: String, summary: String, value: Boolean, onChange: (Boolean) -> Unit) {
    top.yukonga.miuix.kmp.preference.SwitchPreference(title = title, summary = summary, checked = value, onCheckedChange = onChange)
}

@Composable
private fun HomeScreen(state: SkyState, version: String) {
    var notice by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val statusColor = SkyTheme.colors.primary
    val statusBackground = androidx.compose.ui.graphics.lerp(SkyTheme.colors.card, SkyTheme.colors.primary, 0.18f)
    val statusContent: @Composable ColumnScope.() -> Unit = {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("准备就绪", fontSize = 28.sp)
                Text("版本：$version", fontSize = 15.sp)
                Spacer(Modifier.height(16.dp))
                Text("sky", fontSize = 16.sp)
            }
            top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.CheckCircle, "应用状态", modifier = Modifier.size(84.dp), tint = statusColor)
        }
    }
    top.yukonga.miuix.kmp.basic.Card(colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = statusBackground), content = statusContent)
    Panel("公告" + if (state.announcementNew) " · 新公告" else "", state.announcement) {
        if (state.announcementCached) Text("当前显示缓存公告", color = SkyTheme.colors.textSecondary)
        if (state.announcementNew) top.yukonga.miuix.kmp.basic.TextButton(text = "标记已读", onClick = state::markAnnouncementRead)
    }
    if (state.remoteConfig.maintenance.enabled) Panel("维护提示", state.remoteConfig.maintenance.message)
    AuthorizationCard(state)
    ProgramCard(state)
    Panel("", preferenceGroup = true) {
        top.yukonga.miuix.kmp.preference.ArrowPreference(title = "支持开发", summary = "支持 sky 的持续开发", startAction = { top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Favorite, null) }, onClick = { val url = state.remoteConfig.links.support; if (url != null) openWeb(context, url) else notice = "支持开发地址尚未配置。" })
        top.yukonga.miuix.kmp.preference.ArrowPreference(title = "了解 sky", summary = "了解 sky 的使用与开发", startAction = { top.yukonga.miuix.kmp.basic.Icon(Icons.AutoMirrored.Rounded.MenuBook, null) }, onClick = { val url = state.remoteConfig.links.about; if (url != null) openWeb(context, url) else notice = "sky 介绍地址尚未配置。" })
    }
    notice?.let { InfoDialog("sky", it) { notice = null } }

}

@Composable
private fun DriverScreen(state: SkyState, choose: () -> Unit) {
    var show by remember { mutableStateOf(false) }
    val downloads by state.downloadRepository.downloads.collectAsState()
    top.yukonga.miuix.kmp.basic.Card {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Smartphone, "设备", tint = SkyTheme.colors.primary, modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${Build.MANUFACTURER} ${Build.MODEL}", fontSize = 19.sp)
                Text("Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}", color = SkyTheme.colors.textSecondary, fontSize = 13.sp)
                Text("内核：${shortKernel(System.getProperty("os.version") ?: "未知")}", color = SkyTheme.colors.textSecondary, fontSize = 12.sp)
            }
            top.yukonga.miuix.kmp.basic.Surface(color = SkyTheme.colors.error.copy(alpha = 0.12f), shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)) {
                Text("需检查", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = SkyTheme.colors.error, fontSize = 12.sp)
            }
        }
    }
    if (downloads.none { it.status == DriverDownloadStatus.COMPLETED }) top.yukonga.miuix.kmp.basic.Card {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("云端驱动"); Text("尚未下载驱动", color = SkyTheme.colors.textSecondary) }
            top.yukonga.miuix.kmp.basic.TextButton(text = "下载", onClick = { show = true })
        }
    } else Panel("下载管理", "已下载 ${downloads.count { it.status == DriverDownloadStatus.COMPLETED }} 个驱动") {
        Action("管理下载") { show = true }
    }
    Panel("文件加载驱动", "支持 ZIP / .sh · 最大 1024 MiB") {
        Action("选择 ZIP 或脚本", choose)
        if (state.driverName != "未选择驱动文件") Text(state.driverName, fontSize = 13.sp)
        Text("仅选择文件，当前未执行驱动加载。", fontSize = 12.sp)
    }
    if (show) DriverPicker(state) { show = false }
}

@Composable
private fun SettingsScreen(state: SkyState, version: String) {
    var message by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    Panel("版本信息", "sky\n当前版本：$version\n包名：${context.packageName}") {
        Text(state.updateStatus, color = SkyTheme.colors.textSecondary)
        top.yukonga.miuix.kmp.basic.Button(onClick = state::checkUpdate, enabled = !state.updateBusy, modifier = Modifier.fillMaxWidth()) { Text(if (state.updateBusy) "检查中…" else "检查更新") }
        state.updateInfo?.let { update ->
            Text(update.notes)
            if (update.url.isNotEmpty()) Action("下载 ${update.version}" + if (update.forced) "（必须更新）" else "") { openWeb(context, update.url) }
        }
    }
    Panel("", preferenceGroup = true) {
        top.yukonga.miuix.kmp.preference.ArrowPreference(title = "主题设置", summary = "自定义更多主题选项", startAction = { top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Palette, null) }, onClick = { state.page = 4 })
    }
    Panel("", preferenceGroup = true) {
        top.yukonga.miuix.kmp.preference.ArrowPreference(title = "发送日志", startAction = { top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.BugReport, null) }, onClick = {
            try { shareDiagnostics(context, version, state) } catch (_: android.content.ActivityNotFoundException) { message = "没有可用的分享应用。" }
        })
        top.yukonga.miuix.kmp.preference.ArrowPreference(title = "关于", startAction = { top.yukonga.miuix.kmp.basic.Icon(Icons.Rounded.Info, null) }, onClick = { state.page = 5 })
    }
    Panel("", preferenceGroup = true) {
        top.yukonga.miuix.kmp.preference.ArrowPreference(title = "终端", summary = "以 root 执行命令或 .sh 脚本", onClick = { state.page = 7 })
    }
    Panel("", preferenceGroup = true) {
        top.yukonga.miuix.kmp.preference.ArrowPreference(title = "调试", summary = "界面、导航和转场选项", onClick = { state.page = 6 })
    }
    message?.let { InfoDialog("sky", it) { message = null } }
}

@Composable
internal fun InfoDialog(title: String, message: String, close: () -> Unit) {
    top.yukonga.miuix.kmp.overlay.OverlayDialog(show = true, title = title, onDismissRequest = close) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(message, Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()))
            top.yukonga.miuix.kmp.basic.TextButton(text = "确定", onClick = close, modifier = Modifier.fillMaxWidth())
        }
    }
}


internal fun shortKernel(value: String): String = Regex("^(.+?-android\\d+-\\d+)").find(value)?.groupValues?.get(1) ?: value
internal fun openWeb(context: android.content.Context, url: String) {
    if (!isHttpsUrl(url)) return
    try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))) }
    catch (_: android.content.ActivityNotFoundException) { android.widget.Toast.makeText(context, "没有可用浏览器", android.widget.Toast.LENGTH_SHORT).show() }
}
