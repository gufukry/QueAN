package com.quan.quan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private val TABS = listOf("启动", "设备", "配置", "指南")
private val ICONS = listOf(Icons.Rounded.Cottage, Icons.Rounded.Smartphone,
        Icons.Rounded.Settings, Icons.Rounded.MenuBook)

@Composable
fun QueApp(state: AppState) {
    val controller = remember(state.dark) {
        ThemeController(if (state.dark) ColorSchemeMode.Dark else ColorSchemeMode.Light, isDark = state.dark)
    }
    MiuixTheme(controller = controller) {
        val c = MiuixTheme.colorScheme
        val colors = (if (state.dark) darkColorScheme() else lightColorScheme()).copy(
            primary = c.primary, onPrimary = c.onPrimary,
            background = c.background, onBackground = c.onBackground,
            surface = c.surface, onSurface = c.onSurface,
            surfaceVariant = c.surfaceContainer, onSurfaceVariant = c.onSurface,
        )
        MaterialTheme(colorScheme = colors) {
            Surface(Modifier.fillMaxSize(), color = colors.background) {
                LaunchedEffect(Unit) {
                    state.kernelVer = state.kvNow()
                    state.refreshStatus()
                    state.scanDrivers()
                }
                MainScreen(state, colors.background)
            }
        }
    }
}

@Composable
private fun MainScreen(state: AppState, bg: androidx.compose.ui.graphics.Color) {
    val backdrop = rememberLayerBackdrop { drawRect(bg); drawContent() }
    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            Text("QueAN - 鹊安", fontSize = 30.sp, modifier = Modifier.padding(start = 22.dp, top = 14.dp))
            Text("内核启动器 · Compose", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 22.dp, bottom = 6.dp))
            Column(
                Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (state.page) {
                    0 -> HomeScreen(state)
                    1 -> DeviceScreen(state)
                    2 -> SettingsScreen(state)
                    else -> GuideScreen()
                }
                Spacer(Modifier.height(96.dp))
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
                        top.yukonga.miuix.kmp.basic.Icon(imageVector = ICONS[index], contentDescription = label)
                        top.yukonga.miuix.kmp.basic.Text(text = label, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun Panel(title: String, content: @Composable ColumnScope.() -> Unit) {
    top.yukonga.miuix.kmp.basic.Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun Row2(k: String, v: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(k, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(v, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HomeScreen(state: AppState) {
    Panel("运行状态") {
        Row2("内核", if (state.online) "在线" else "离线")
        Row2("目标游戏", state.gameNames[state.game])
        Row2("初始化方式", listOf("自动", "普通", "内核")[state.mode])
    }
    top.yukonga.miuix.kmp.basic.Button(
        onClick = { if (state.online) state.stopKernel() else state.startKernel() },
        modifier = Modifier.fillMaxWidth()
    ) {
        top.yukonga.miuix.kmp.basic.Text(if (state.online) "停止内核" else "初始化并启动")
    }
    if (state.log.isNotEmpty()) {
        Panel("日志") {
            Text(state.log, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DeviceScreen(state: AppState) {
    Panel("驱动通道") {
        listOf("twt", "paradise", "kairos").forEach { name ->
            top.yukonga.miuix.kmp.basic.Button(onClick = { state.updateDrv(name) },
                modifier = Modifier.fillMaxWidth()) {
                top.yukonga.miuix.kmp.basic.Text(if (state.drv == name) "✓ $name" else name)
            }
        }
        Row2("已选脚本", state.drvSel.ifBlank { "自动匹配中…" })
        Row2("驱动状态", state.drvState)
        Row2("内核版本", state.kernelVer)
    }
    Panel("可用驱动 (${state.drvList.size})") {
        if (state.drvList.isEmpty()) {
            Text("assets/drivers/${state.drv} 下没有脚本", fontSize = 13.sp)
        } else {
            state.drvList.forEach { n ->
                top.yukonga.miuix.kmp.basic.Button(
                    onClick = { state.drvSel = n },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    top.yukonga.miuix.kmp.basic.Text(if (state.drvSel == n) "✓ $n" else n)
                }
            }
        }
    }
    top.yukonga.miuix.kmp.basic.Button(onClick = { state.runDriver() },
        modifier = Modifier.fillMaxWidth()) {
        top.yukonga.miuix.kmp.basic.Text("执行驱动")
    }
    Panel("设备信息") {
        Row2("机型", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        Row2("系统", "Android ${android.os.Build.VERSION.RELEASE} · API ${android.os.Build.VERSION.SDK_INT}")
        Row2("内核版本", System.getProperty("os.version") ?: "-")
        Row2("Root", if (state.sh("id -u").trim() == "0") "已授权" else "未授权")
    }
}

@Composable
private fun SettingsScreen(state: AppState) {
    Panel("外观") {
        Row2("深色模式", if (state.dark) "开" else "关")
        top.yukonga.miuix.kmp.basic.Button(onClick = { state.updateDark(!state.dark) },
            modifier = Modifier.fillMaxWidth()) {
            top.yukonga.miuix.kmp.basic.Text("切换主题")
        }
    }
    Panel("目标游戏") {
        state.gameNames.forEachIndexed { i, name ->
            top.yukonga.miuix.kmp.basic.Button(onClick = { state.updateGame(i) },
                modifier = Modifier.fillMaxWidth()) {
                top.yukonga.miuix.kmp.basic.Text(if (state.game == i) "✓ $name" else name)
            }
        }
    }
    Panel("初始化方式") {
        listOf("自动", "普通", "内核").forEachIndexed { i, name ->
            top.yukonga.miuix.kmp.basic.Button(onClick = { state.updateMode(i) },
                modifier = Modifier.fillMaxWidth()) {
                top.yukonga.miuix.kmp.basic.Text(if (state.mode == i) "✓ $name" else name)
            }
        }
    }
}

@Composable
private fun GuideScreen() {
    Panel("使用说明") {
        Text("1. 首页点「初始化并启动」，脚本会以 root 执行。", fontSize = 13.sp)
        Text("2. 驱动与内核资源放在 assets/loader/ 下。", fontSize = 13.sp)
        Text("3. 底栏为液态玻璃（Miuix + Kyant 折射着色器）。", fontSize = 13.sp)
        Text("4. 需要 Android 13 及以上才有完整玻璃效果。", fontSize = 13.sp)
    }
}
