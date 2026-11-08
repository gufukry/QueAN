package com.quan.quan

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
        val colors = (if (state.dark) darkColorScheme() else lightColorScheme()).copy(
            primary = c.primary, onPrimary = c.onPrimary,
            background = c.background, onBackground = c.onBackground,
            surface = c.surface, onSurface = c.onSurface,
            surfaceVariant = c.surfaceContainer, onSurfaceVariant = c.onSurface
        )
        MaterialTheme(colorScheme = colors) {
            var splash by remember { mutableStateOf(true) }
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
                if (state.updatePrompt) {
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
                if (state.showKernelPrompt) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { },
                        title = { Text("首次使用", fontWeight = FontWeight.SemiBold) },
                        text = {
                            Text(
                                "还没有下载内核本体。需要先下载一次才能初始化，下载完会自动替换并生效。",
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Text(
                                "立即下载", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable {
                                        state.upGame = state.game
                                        state.openUpdatePage(state.game)
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
    val appear = remember { Animatable(0f) }
    val word = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(420))
        word.animateTo(1f, tween(360))
        kotlinx.coroutines.delay(480)
        exit.animateTo(1f, tween(420))     // 平滑淡出，不用瞬切
        done()
    }

    val a = 1f - exit.value
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = a))) {
        Column(
            Modifier.fillMaxSize().alpha(a),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(104.dp)
                    .graphicsLayer {
                        scaleX = 0.86f + 0.14f * appear.value
                        scaleY = scaleX
                        alpha = appear.value
                    }
                    .background(Color.White, RoundedCornerShape(26.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (logo != null) {
                    Image(logo, null, modifier = Modifier.fillMaxSize())
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "QueAN", color = Color.White.copy(alpha = word.value),
                fontSize = 24.sp, fontWeight = FontWeight.Bold
            )
            Text(
                "内核启动器", color = Color(0xFF8E8E93).copy(alpha = word.value), fontSize = 13.sp
            )
        }
    }
}

/* ---------------- 主界面 ---------------- */

@Composable
private fun MainScreen(state: AppState, bg: Color) {
    val backdrop = rememberLayerBackdrop { drawRect(bg); drawContent() }
    val titles = listOf("启动", "运行环境", "配置管理", "使用指南")
    val subs = listOf(
        "选择初始化方式，输入卡密后开始使用。",
        "查看设备信息与当前运行状态。",
        "保存当前参数，或导入已有配置。",
        "了解初始化流程与配置保存方法。"
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
        CardTitle("开始使用")
        Text("单码卡密", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            Modifier.fillMaxWidth().height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (state.kami.isEmpty()) {
                Text(
                    "请输入卡密", fontSize = 15.sp,
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
                Text("记住卡密", fontSize = 16.sp)
                Text(
                    "下次打开时自动填写", fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = state.remember, onCheckedChange = { state.updateRemember(it) })
        }
        DarkButton(
            if (state.licensed) "重新验证并启动" else "初始化并启动",
            enabled = !state.logging
        ) { state.startFlow() }
        Body("选择方式只保存设置，点击上方按钮后才执行初始化。")
        if (state.loginMsg.isNotBlank()) {
            Body(
                if (state.licensed) "已登录 ${state.expireText}" else state.loginMsg,
                color = if (state.licensed) Color(0xFF00B578) else Color(0xFFFA5151)
            )
        }
        if (state.licensed) {
            Body("当前卡密：${state.kami}", size = 12)
            LightButton("退出登录") { state.logout() }
        }
    }

    CardC {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CardTitle("运行模式")
            Spacer(Modifier.weight(1f))
            ChipC(
                if (state.online) "运行中" else "需检查",
                if (state.online) Color(0xFF00B578) else Color(0xFFFA5151)
            )
        }
        OptionRadio("内核ROOT模式", "保留现有内核驱动与 ELF 核心流程", true) { }
        if (state.online) LightButton("停止内核") { state.stopKernel() }
    }

    LogCard(state)

    CardC {
        CardTitle("内核初始化方式")
        val names = listOf("自动初始化", "普通初始化", "内核初始化")
        names.forEachIndexed { i, n ->
            OptionRadio(n, "", state.mode == i) { state.updateMode(i) }
        }
        Body("自动检测可用初始化入口；初始化成功后才启动核心。")
        HairLine()
        Text("当前内核", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(state.kernelVer, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Body("匹配对应内核版本并检测加载能力；实际加载失败不会反复尝试或启动核心。")
    }

    CardC {
        CardTitle("启动小贴士")
        Body("等待初始化与验证完成后，再手动进入游戏。")
    }
}

/* ---------------- 设备页 ---------------- */

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
            if (state.online) "正常" else "需检查",
            if (state.online) Color(0xFF00B578) else Color(0xFFFA5151)
        )
        Body(if (state.online) "核心运行状态 正常。" else "检查核心运行状态 失败。")
    }

    CardC {
        CardTitle("系统信息")
        KvRow(
            "Android",
            "${android.os.Build.VERSION.RELEASE} · API ${android.os.Build.VERSION.SDK_INT}"
        )
        HairLine()
        KvRow("内核版本", state.kernelVer)
        HairLine()
        KvRow("本机内核", System.getProperty("os.version") ?: "-")
        HairLine()
        KvRow(
            "核心状态", if (state.online) "运行中" else "需要检查",
            if (state.online) Color(0xFF00B578) else Color(0xFFFA5151)
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
        DarkButton("执行驱动") { state.runDriver() }
    }
}

/* ---------------- 配置页 ---------------- */

@Composable
private fun ConfigPage(state: AppState) {
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
