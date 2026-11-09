package com.sky.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TermBg = Color(0xFF101012)
private val TermFg = Color(0xFFE3E3E8)
private val TermDim = Color(0xFF8B8B92)

/** 终端页：以 root 执行命令，也可以直接运行 .sh 脚本 */
@Composable
internal fun TerminalScreen(state: SkyState) {
    val scroll = rememberScrollState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) state.runTerminalScript(uri)
    }
    LaunchedEffect(state.termLog) { scroll.animateScrollTo(scroll.maxValue) }

    Panel("终端", "以 root 执行命令，也可以直接运行 .sh 脚本") {
        Box(
            Modifier.fillMaxWidth().height(340.dp)
                .background(TermBg, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(Modifier.verticalScroll(scroll)) {
                Text(
                    state.termLog,
                    fontSize = 12.sp,
                    color = TermFg,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.weight(1f).height(46.dp)
                    .background(Color(0xFF1C1C1F), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (state.termInput.isEmpty()) {
                    Text("输入命令", fontSize = 13.sp, color = TermDim, fontFamily = FontFamily.Monospace)
                }
                BasicTextField(
                    value = state.termInput,
                    onValueChange = state::updateTermInput,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = TermFg, fontFamily = FontFamily.Monospace),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { state.runTerminalCommand() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            top.yukonga.miuix.kmp.basic.Button(
                onClick = { state.runTerminalCommand() },
                enabled = !state.termRunning,
                modifier = Modifier.height(46.dp).width(80.dp)
            ) { Text("执行", fontSize = 14.sp) }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            top.yukonga.miuix.kmp.basic.TextButton(text = "运行脚本", onClick = { picker.launch(arrayOf("*/*")) })
            top.yukonga.miuix.kmp.basic.TextButton(text = "清空", onClick = state::clearTerminal)
            Spacer(Modifier.weight(1f))
            Text(if (state.termRunning) "执行中…" else "su 权限", fontSize = 12.sp, color = TermDim)
        }
    }
}
