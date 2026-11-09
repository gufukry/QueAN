package com.sky.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog

@Composable
internal fun ProgramCard(state: SkyState) {
    var show by remember { mutableStateOf(false) }
    val program by state.program.state.collectAsState()
    val context = LocalContext.current
    val scroll = rememberScrollState()
    LaunchedEffect(program.lines.size, show) { if (show) scroll.animateScrollTo(scroll.maxValue) }
    Panel("主程序") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("v1.0", fontSize = 22.sp)
                Text("主程序尚未配置", color = SkyTheme.colors.textSecondary, fontSize = 13.sp)
            }
            TextButton(text = "复制目录", onClick = { copyText(context, "/data/adb/sky/") })
        }
        Text("工作目录：/data/adb/sky/", color = SkyTheme.colors.textSecondary, fontSize = 13.sp)
        TextField(value = state.programArguments, onValueChange = { state.programArguments = it }, label = "参数预输入（每行一个参数）", modifier = Modifier.fillMaxWidth())
        Text("参数仅作为独立参数预留，不作为 Shell 命令执行。", color = SkyTheme.colors.textSecondary, fontSize = 12.sp)
        Action("启动主程序") {
            state.program.start(state.programArguments.lineSequence().filter { it.isNotEmpty() }.toList())
            show = true
        }
    }
    if (show) OverlayDialog(show = true, title = when (program.phase) {
        ProgramPhase.READY -> "等待启动"
        ProgramPhase.RUNNING -> "运行中"
        ProgramPhase.COMPLETED -> "执行完成"
        ProgramPhase.FAILED -> "启动失败"
        ProgramPhase.UNCONFIGURED -> "尚未配置"
        ProgramPhase.CANCELLED -> "已取消"
    }, onDismissRequest = { show = false }) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectionContainer {
                Column(Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 420.dp).verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    program.lines.forEachIndexed { index, line ->
                        Row {
                            Text("${index + 1}", Modifier.width(32.dp), color = SkyTheme.colors.textMuted, fontSize = 12.sp)
                            Text(line, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        }
                    }
                }
            }
            program.exitCode?.let { Text("退出码：$it", color = if (it == 0) SkyTheme.colors.success else SkyTheme.colors.error) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(text = "复制", onClick = { copyText(context, program.lines.joinToString("\n")) })
                TextButton(text = "清空", onClick = state.program::clear)
                TextButton(text = "关闭", onClick = { show = false })
            }
        }
    }
}

private fun copyText(context: Context, value: String) {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("sky", value))
}
