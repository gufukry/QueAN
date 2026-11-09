package com.quan.quan

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.weishu.kernelsu.ui.component.FloatingBottomBar
import me.weishu.kernelsu.ui.component.FloatingBottomBarItem
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

private val TABS = listOf("首页", "设备", "配置", "指南")
private val ICONS = listOf(
    Icons.Rounded.Cottage, Icons.Rounded.Smartphone,
    Icons.Rounded.Settings, Icons.Rounded.MenuBook
)

@Composable
fun QueApp(state: AppState) {
    val controller = remember(state.dark) {
        ThemeController(
            if (state.dark) ColorSchemeMode.Dark else ColorSchemeMode.Light,
            isDark = state.dark
        )
    }
    MiuixTheme(controller = controller) {
        val c = MiuixTheme.colorScheme
        val darkNow = state.dark
        val anim = tween<Color>(durationMillis = 420)
        val bg by animateColorAsState(
            if (darkNow) Color(0xFF000000) else Color(0xFFF2F3F5), anim, label = "bg"
        )
        val sf by animateColorAsState(
            if (darkNow) Color(0xFF1C1C1E) else Color(0xFFFFFFFF), anim, label = "surface"
        )
        val sv by animateColorAsState(
            if (darkNow) Color(0xFF2C2C2E) else Color(0xFFF5F5F7), anim, label = "surfaceVariant"
        )
        val onSf by animateColorAsState(
            if (darkNow) Color(0xFFF2F2F2) else Color(0xFF0D0D0D), anim, label = "onSurface"
        )
        val onSv by animateColorAsState(
            if (darkNow) Color(0xFF8C8C8C) else Color(0xFF808080), anim, label = "onSurfaceVariant"
        )
        val colors = if (darkNow) darkColorScheme(
            primary = c.primary, onPrimary = c.onPrimary,
            background = bg, onBackground = onSf,
            surface = sf, onSurface = onSf,
            surfaceVariant = sv, onSurfaceVariant = onSv
        ) else lightColorScheme(
            primary = c.primary, onPrimary = c.onPrimary,
            background = bg, onBackground = onSf,
            surface = sf, onSurface = onSf,
            surfaceVariant = sv, onSurfaceVariant = onSv
        )
        MaterialTheme(colorScheme = colors) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides colors.onBackground
            ) {
            var splash by remember { mutableStateOf(true) }
            var uiReady by remember { mutableStateOf(false) }
            Box(Modifier.fillMaxSize().background(colors.background)) {
                // 主页一直在下面渲染，启动层只负责盖住并淡出，收尾才不会卡一下
                MainScreen(state, colors.background)
                LaunchedEffect(Unit) {
                    state.kernelVer = state.kvNow()
                    state.refreshStatus()
                    state.scanDrivers()
                    state.checkUpdate()
                    state.checkFirstRun()
                    state.autoCheckUpdate()
                }
                if (splash) SplashOverlay { splash = false }
                // 入场动画结束、稍等一下再弹窗，避免和淡出叠在一起
                LaunchedEffect(splash) {
                    if (!splash) {
                        kotlinx.coroutines.delay(600)
                        uiReady = true
                    }
                }
                if (state.driverDialog) {
                    val dScroll = rememberScrollState()
                    LaunchedEffect(state.driverLog) { dScroll.animateScrollTo(dScroll.maxValue) }
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { state.closeDriverDialog() },
                        title = { Text("刷驱动", fontWeight = FontWeight.SemiBold) },
                        text = {
                            Column {
                                Text(
                                    "${state.drv} · ${state.drvSel}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                Box(
                                    Modifier.fillMaxWidth().height(52.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (state.driverInput.isEmpty()) {
                                        Text(
                                            "输入菜单序号，默认 1",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    BasicTextField(
                                        value = state.driverInput,
                                        onValueChange = { state.driverInput = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Box(
                                    Modifier.fillMaxWidth().height(150.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Column(Modifier.verticalScroll(dScroll)) {
                                        Text(
                                            if (state.driverLog.isBlank()) "日志会显示在这里"
                                            else state.driverLog,
                                            fontSize = 12.sp, lineHeight = 17.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = if (state.driverLog.isBlank())
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            else Color(0xFF3A7D44)
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Text(
                                "执行", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable { state.runDriverManual() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        },
                        dismissButton = {
                            Text(
                                "关闭", fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clickable { state.closeDriverDialog() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    )
                }
                if (uiReady && state.updatePrompt) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { state.dismissUpdatePrompt() },
                        title = { Text("发现新版本", fontWeight = FontWeight.SemiBold) },
                        text = {
                            Text(
                                "${state.gameNames[state.promptGame]} 内核 v${state.promptVer} 已发布，前往更新页下载？",
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Text(
                                "前往更新", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable { state.gotoUpdateFromPrompt() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        },
                        dismissButton = {
                            Text(
                                "稍后", fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clickable { state.dismissUpdatePrompt() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    )
                }
                if (uiReady && state.showKernelPrompt) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { },
                        title = { Text("首次使用", fontWeight = FontWeight.SemiBold) },
                        text = {
                            Column {
                                Text(
                                    "还没有下载内核本体。先选要下载哪个游戏的内核：",
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(10.dp))
                                state.gameNames.forEachIndexed { i, name ->
                                    OptionRadio(name, "", state.upGame == i) {
                                        state.upGame = i
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Text(
                                "立即下载", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable {
                                        state.showKernelPrompt = false
                                        state.openUpdatePage(state.upGame)
                                        state.startUpdate()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        },
                        dismissButton = {
                            Text(
                                "稍后", fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clickable { state.dismissKernelPrompt() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    )
                }
            }
            }
        }
    }
}

/* ---------------- 开场动画 ---------------- */

@Composable
private fun SplashOverlay(done: () -> Unit) {
    val ctx = LocalContext.current
    val logo = remember {
        try {
            val bytes = ctx.assets.open("launcher.png").readBytes()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
        } catch (t: Throwable) {
            null
        }
    }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        t.animateTo(1f, tween(3100, easing = LinearEasing))
        done()
    }
    val p = t.value
    fun seg(start: Float, len: Float) = ((p - start) / len).coerceIn(0f, 1f)

    val ringIn = seg(0.00f, 0.30f)
    val iconIn = seg(0.05f, 0.26f)
    val markIn = seg(0.12f, 0.38f)
    val subIn = seg(0.50f, 0.24f)
    val dashIn = seg(0.68f, 0.16f)
    val out = seg(0.91f, 0.09f)
    val a = 1f - out

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = a)),
        contentAlignment = Alignment.Center
    ) {
        // HUD 细圆环 + 旋转的小点
        Canvas(Modifier.size(300.dp).alpha(a * ringIn)) {
            val r = size.minDimension / 2f - 6.dp.toPx()
            drawCircle(Color(0x30FFFFFF), radius = r, style = Stroke(1.dp.toPx()))
            drawCircle(Color(0x18FFFFFF), radius = r * 0.84f, style = Stroke(1.dp.toPx()))
            val base = -90f + p * 150f
            for (k in 0..2) {
                val rad = Math.toRadians((base + k * 120f).toDouble())
                drawCircle(
                    Color(0x70FFFFFF), 2.5.dp.toPx(),
                    Offset(
                        center.x + (r * Math.cos(rad)).toFloat(),
                        center.y + (r * Math.sin(rad)).toFloat()
                    )
                )
            }
            val w = size.width
            drawLine(Color(0x22FFFFFF), Offset(0f, center.y), Offset(w * 0.10f, center.y), 1.dp.toPx())
            drawLine(Color(0x22FFFFFF), Offset(w * 0.90f, center.y), Offset(w, center.y), 1.dp.toPx())
        }

        Column(
            Modifier.fillMaxSize().alpha(a),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (logo != null) {
                Box(
                    Modifier.size(76.dp)
                        .graphicsLayer {
                            alpha = iconIn
                            scaleX = 0.8f + 0.2f * iconIn
                            scaleY = scaleX
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(logo, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Spacer(Modifier.height(22.dp))
            }

            // 字标：逐字模糊聚焦 + 轻微上浮
            Row(horizontalArrangement = Arrangement.Center) {
                val word = "QueAN"
                word.forEachIndexed { i, ch ->
                    val li = ((markIn - i * 0.10f) / 0.55f).coerceIn(0f, 1f)
                    Text(
                        ch.toString(),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = li),
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = (1f - li) * 26f
                                scaleX = 1f + (1f - li) * 0.12f
                                scaleY = scaleX
                            }
                            .blur(16.dp * (1f - li), edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "内核启动器",
                fontSize = 13.sp,
                letterSpacing = (1f + 8f * subIn).sp,
                color = Color(0xFF8E8E93).copy(alpha = subIn)
            )

            Spacer(Modifier.height(34.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(5) { i ->
                    val di = ((dashIn - i * 0.12f) / 0.5f).coerceIn(0f, 1f)
                    Box(
                        Modifier.size(width = 14.dp, height = 2.dp)
                            .background(Color(0xFFBFBFBF).copy(alpha = di * 0.85f), RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}

/* ---------------- 主界面 ---------------- */

@Composable
private fun MainScreen(state: AppState, bg: Color) {
    val backdrop = rememberLayerBackdrop { drawRect(bg); drawContent() }
    val titles = listOf("启动", "本机", "设置", "说明")
    val subs = listOf(
        "填好卡密即可一键初始化并启动内核。",
        "本机信息、驱动与内核状态一览。",
        "切换游戏、导入配置、检查内核更新。",
        "启动流程与常见问题。"
    )

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("QueAN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(
                    if (state.dark) "浅色模式" else "深色模式",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { state.updateDark(!state.dark) }
                )
            }
            Text(
                titles[state.page], fontSize = 32.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                subs[state.page], fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            AnimatedContent(
                targetState = state.page,
                transitionSpec = {
                    // 往右边的页签切 → 新页从右侧推进来；往左返回 → 从左侧
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(260)) { full -> dir * full } +
                            fadeIn(tween(180))) togetherWith
                            (slideOutHorizontally(tween(260)) { full -> -dir * full } +
                                    fadeOut(tween(180)))
                },
                modifier = Modifier.fillMaxWidth().weight(1f),
                label = "page"
            ) { page ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (page) {
                        0 -> HomePage(state)
                        1 -> DevicePage(state)
                        2 -> ConfigPage(state)
                        else -> GuidePage()
                    }
                    Spacer(Modifier.height(96.dp))
                }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter).padding(horizontal = 24.dp, vertical = 10.dp)) {
            FloatingBottomBar(
                selectedIndex = state.page,
                onSelected = { state.page = it },
                backdrop = backdrop,
                tabsCount = TABS.size,
                isBlurEnabled = true
            ) { activate ->
                TABS.forEachIndexed { index, label ->
                    FloatingBottomBarItem(
                        selected = state.page == index,
                        onClick = { activate(index) },
                        modifier = Modifier.defaultMinSize(minWidth = 64.dp)
                    ) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            imageVector = ICONS[index], contentDescription = label
                        )
                        top.yukonga.miuix.kmp.basic.Text(
                            text = label, fontSize = 11.sp, maxLines = 1
                        )
                    }
                }
            }
        }
        if (state.showUpdatePage) {
            UpdatePage(state)
        }
        if (state.showTerminal) {
            TerminalPage(state)
        }
    }
}

/* ---------------- 终端 ---------------- */

@Composable
private fun TerminalPage(state: AppState) {
    val scroll = rememberScrollState()
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) state.runScript(uri) }
    LaunchedEffect(state.termLog) { scroll.animateScrollTo(scroll.maxValue) }

    Column(
        Modifier.fillMaxSize()
            .background(Color(0xFF0B0B0C))
            .safeDrawingPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "‹ 返回", fontSize = 15.sp, color = Color(0xFF8E8E93),
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { state.closeTerminal() }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                "清空", fontSize = 14.sp, color = Color(0xFF8E8E93),
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { state.clearTerminal() }
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            )
        }
        Text(
            "终端", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            Modifier.fillMaxWidth().weight(1f)
                .background(Color(0xFF141416), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column(Modifier.verticalScroll(scroll)) {
                Text(
                    if (state.termLog.isBlank()) "> 等待命令" else state.termLog,
                    fontSize = 12.sp, lineHeight = 17.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = Color(0xFFD7D7DC)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("\$", fontSize = 15.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFF00B578))
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.weight(1f).height(48.dp)
                    .background(Color(0xFF1C1C1E), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (state.termInput.isEmpty()) {
                    Text(
                        "输入命令", fontSize = 14.sp, color = Color(0xFF6E6E73),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                BasicTextField(
                    value = state.termInput,
                    onValueChange = { state.termInput = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp, color = Color(0xFFF2F2F2),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Send
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = { state.runTerm() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.height(48.dp).width(64.dp)
                    .background(
                        if (state.termRunning) Color(0xFF3A3A3C) else Color(0xFF00B578),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = !state.termRunning) { state.runTerm() },
                contentAlignment = Alignment.Center
            ) {
                Text("运行", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "执行脚本（.sh）", fontSize = 14.sp, color = Color(0xFF00B578),
                modifier = Modifier
                    .clickable { picker.launch("*/*") }
                    .padding(vertical = 6.dp, horizontal = 6.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                "以 su 执行", fontSize = 12.sp, color = Color(0xFF6E6E73)
            )
        }
    }
}

/* ---------------- 更新页 ---------------- */

@Composable
private fun UpdatePage(state: AppState) {
    Column(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "‹ 返回", fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { state.closeUpdatePage() }
                .padding(vertical = 8.dp, horizontal = 4.dp)
        )
        Text(
            "内核更新", fontSize = 32.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        CardC {
            CardTitle("目标游戏")
            state.gameNames.forEachIndexed { i, name ->
                OptionRadio(name, "", state.upGame == i) {
                    state.upGame = i
                    state.checkUpdate()
                }
            }
        }

        CardC {
            CardTitle("版本")
            KvRow(
                "当前内核",
                if (state.hasKernel(state.upGame)) "v" + state.localKernelVer(state.upGame)
                else "未下载"
            )
            KvRow("最新版本", if (state.upVer.isBlank()) "-" else "v" + state.upVer)
            Body(state.upStatus)
            if (state.downloading) Body("下载中 ${(state.progress * 100).toInt()}%")
            DarkButton(if (state.downloading) "下载中…" else "立即更新", enabled = !state.downloading) {
                state.startUpdate()
            }
        }

        if (state.noticeText.isNotBlank()) {
            CardC {
                CardTitle("更新内容")
                Body(state.noticeText)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/* ---------------- 启动页 ---------------- */

@Composable
private fun LogCard(state: AppState) {
    val scroll = rememberScrollState()
    LaunchedEffect(state.log) { scroll.animateScrollTo(scroll.maxValue) }
    CardC {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CardTitle("日志")
            Spacer(Modifier.weight(1f))
            Text(
                "清空", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { state.clearLog() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Box(
            Modifier.fillMaxWidth().height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column(Modifier.verticalScroll(scroll)) {
                Text(
                    if (state.log.isBlank()) "暂无日志" else state.log,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = if (state.log.isBlank())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else Color(0xFF3A7D44)
                )
            }
        }
    }
}

@Composable
private fun HomePage(state: AppState) {
    CardC {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CardTitle("当前状态")
            Spacer(Modifier.weight(1f))
            ChipC(
                if (state.online) "运行中" else state.statusText(),
                if (state.online || state.statusOk()) Color(0xFF00B578) else Color(0xFFFA5151)
            )
        }
        HairLine()
        StackedRow("目标游戏", state.gameNames[state.game])
        HairLine()
        StackedRow("内核版本", state.kernelVer)
        HairLine()
        StackedRow("驱动通道", state.drv + " · " + state.drvSel.ifBlank { "待选择" })
    }

    CardC {
        CardTitle("卡密")
        Box(
            Modifier.fillMaxWidth().height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (state.kami.isEmpty()) {
                Text(
                    "在此粘贴卡密", fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = state.kami,
                onValueChange = { state.saveKami(it) },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("自动填写", fontSize = 16.sp)
                Text(
                    "下次打开时自动填入卡密", fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = state.remember, onCheckedChange = { state.updateRemember(it) })
        }
        DarkButton(
            if (state.licensed) "重新验证并启动" else "一键初始化",
            enabled = !state.logging
        ) { state.startFlow() }
        Body("上面只是保存设置，点按钮才会刷驱动并起内核。")
        if (state.loginMsg.isNotBlank()) {
            Body(
                if (state.licensed) "已登录 ${state.expireText}" else state.loginMsg,
                color = if (state.licensed) Color(0xFF00B578) else Color(0xFFFA5151)
            )
        }
        if (state.licensed) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Body("当前卡密 ${state.kami}", size = 12)
                Spacer(Modifier.weight(1f))
                Text(
                    "退出", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { state.logout() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }

    CardC {
        CardTitle("初始化方式")
        val names = listOf("自动初始化", "普通初始化", "内核初始化")
        names.forEachIndexed { i, n ->
            OptionRadio(n, "", state.mode == i) { state.updateMode(i) }
        }
        Body("自动挑可用的入口，初始化成功后才拉起核心。")
    }

    CardC {
        CardTitle("运行方式")
        OptionRadio("内核ROOT模式", "使用内核驱动与 ELF 核心", true) { }
        HairLine()
        Text("当前内核", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(state.kernelVer, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        if (state.online) {
            Spacer(Modifier.height(4.dp))
            LightButton("停止内核") { state.stopKernel() }
        }
    }

    LogCard(state)

    CardC {
        CardTitle("提示")
        Body("初始化完成后回到桌面，再进入游戏即可看到悬浮窗。")
    }
}

@Composable
private fun DevicePage(state: AppState) {
    CardC {
        androidx.compose.material3.Icon(
            Icons.Rounded.Smartphone, null,
            modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            fontSize = 24.sp, fontWeight = FontWeight.Bold
        )
        Body("当前连接的 Android 设备")
        ChipC(
            if (state.online) "运行中" else state.statusText(),
            if (state.online || state.statusOk()) Color(0xFF00B578) else Color(0xFFFA5151)
        )
        Body(
            when {
                state.online -> "核心运行状态 正常。"
                state.statusOk() -> "Root 权限已获取，可以初始化。"
                else -> "检查 Root 权限 / 核心运行状态 失败。"
            }
        )
    }

    CardC {
        CardTitle("系统信息")
        StackedRow(
            "Android",
            "${android.os.Build.VERSION.RELEASE} · API ${android.os.Build.VERSION.SDK_INT}"
        )
        HairLine()
        StackedRow("内核版本", state.kernelVer)
        HairLine()
        StackedRow("本机内核", System.getProperty("os.version") ?: "-")
        HairLine()
        StackedRow(
            "核心状态", if (state.online) "运行中" else state.statusText(),
            if (state.online || state.statusOk()) Color(0xFF00B578) else Color(0xFFFA5151)
        )
        LightButton("复制设备信息") { state.copyDevice() }
    }

    CardC {
        CardTitle("驱动")
        listOf("twt", "paradise", "kairos").forEach { name ->
            OptionRadio(name, "", state.drv == name) { state.updateDrv(name) }
        }
        KvRow("已选脚本", state.drvSel.ifBlank { "自动匹配中…" })
        KvRow("驱动状态", state.drvState)
        state.drvList.forEach { n ->
            OptionRadio(n, "", state.drvSel == n) { state.drvSel = n }
        }
        DarkButton("执行驱动") { state.openDriverDialog() }
    }
}

/* ---------------- 配置页 ---------------- */

@Composable
private fun ConfigPage(state: AppState) {
    CardC {
        CardTitle("终端")
        Body("直接用 root 权限执行命令或脚本，不用改源码。")
        LightButton("打开终端") { state.openTerminal() }
    }

    CardC {
        CardTitle("切换游戏")
        state.gameNames.forEachIndexed { i, name ->
            OptionRadio(name, "", state.game == i) {
                state.updateGame(i)
                state.checkUpdate()
            }
        }
        Body("切换后会按对应游戏读取内核文件与云更新版本。")
    }

    CardC {
        CardTitle("内核配置")
        Body("输入配置码（HXCFG1.…）或服务器数字码，导入后写入内核读取位置。")
        Box(
            Modifier.fillMaxWidth().height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (state.cfgInput.isEmpty()) {
                Text(
                    "粘贴配置码 / 数字码", fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = state.cfgInput,
                onValueChange = { state.cfgInput = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        DarkButton("导入配置") { state.importKernelCfg() }
        if (state.cfgStatus.isNotBlank()) Body(state.cfgStatus)
    }

    CardC {
        androidx.compose.material3.Icon(
            Icons.Rounded.Settings, null,
            modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurface
        )
        CardTitle("本地配置")
        Body("android_config.json")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { LightButton("导出配置") { state.exportConfig() } }
            Box(Modifier.weight(1f)) {
                DarkButton("导入配置") { state.importConfig(state.readClipboard()) }
            }
        }
        Body("可导出当前参数，或从剪贴板导入已有 JSON 配置")
    }

    CardC {
        CardTitle("内核更新")
        KvRow(
            "当前内核",
            if (state.hasKernel(state.game)) "v" + state.localKernelVer(state.game) else "未下载"
        )
        Body("检测到新版会进入更新页；已是最新会直接提示。")
        DarkButton("检查内核更新") {
            state.upGame = state.game
            state.checkAndRoute()
        }
    }

    LogCard(state)

    CardC {
        CardTitle("导入与备份")
        IconRow(
            "保留当前设备路由",
            "不会从配置文件照搬其他设备的驱动路由。",
            Icons.Rounded.Settings
        )
        HairLine()
        IconRow(
            "确认后才导入",
            "导入会先停止核心，验证通过后原子替换；原配置如存在会安全保留。",
            Icons.Rounded.Security
        )
    }
}

/* ---------------- 指南页 ---------------- */

@Composable
private fun GuidePage() {
    CardC {
        IconRow(
            "选择初始化方式",
            "首次使用不默认选择；普通初始化和内核初始化由你决定。",
            Icons.Rounded.Security
        )
        HairLine()
        IconRow("保持联网", "核心启动时需要完成在线验证。", Icons.Rounded.Wifi)
        HairLine()
        IconRow("允许授权", "运行时在系统提示中授予 Root 权限。", Icons.Rounded.Security)
        HairLine()
        IconRow("启动核心", "回到首页完成初始化，等待成功后再手动进入游戏。", Icons.Rounded.Cottage)
    }
    CardC {
        androidx.compose.material3.Icon(
            Icons.Rounded.Info, null,
            modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onSurface
        )
        CardTitle("使用小贴士")
        Body("启动完成后不要重复点击；需要重新加载时，再回到首页执行重新启动。")
        Body("初始化方式会保存在本机。输入卡密后，可自行选择是否记住卡密。")
        Body("更换设备前可在配置页导出参数。导入操作需要确认，且保留当前设备的驱动路由。")
    }
}
