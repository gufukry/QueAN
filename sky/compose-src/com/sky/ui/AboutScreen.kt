package com.sky.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sky.ui.aboutEffect.BgEffectBackground
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
internal fun AboutScreen(version: String, state: SkyState? = null) {
    val context = LocalContext.current
    val tokens = SkyTheme.colors
    val listState = rememberLazyListState()
    var message by remember { mutableStateOf<Pair<String, String>?>(null) }
    val backgroundAlpha = { (1f - (listState.firstVisibleItemScrollOffset / 420f)).coerceIn(0f, 1f) }

    BgEffectBackground(
        dynamicBackground = state?.liquidGlass ?: true,
        isFullSize = true,
        alpha = backgroundAlpha,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().height(340.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier.size(96.dp).background(tokens.primary.copy(alpha = 0.16f), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("s", fontSize = 72.sp, fontWeight = FontWeight.Light, color = tokens.primary)
                    }
                    Text("sky", fontSize = 42.sp, fontWeight = FontWeight.Light, color = tokens.text)
                    Spacer(Modifier.height(10.dp))
                    Text("v$version", fontSize = 16.sp, color = tokens.textSecondary)
                }
            }
            item {
                Card(
                    colors = CardDefaults.defaultColors(tokens.card.copy(alpha = 0.88f), tokens.text),
                ) {
                    ArrowPreference(
                        title = "查看源码",
                        onClick = { message = "源码" to "sky 源码地址尚未配置。" },
                    )
                    ArrowPreference(
                        title = "加入社区",
                        onClick = { message = "社区" to "sky 社区地址尚未配置。" },
                    )
                }
            }
            item {
                Card(
                    colors = CardDefaults.defaultColors(tokens.card.copy(alpha = 0.88f), tokens.text),
                ) {
                    ArrowPreference(
                        title = "KernelSU 许可",
                        endActions = { Text("GPL-3.0", color = tokens.textSecondary) },
                        onClick = { message = "KernelSU · GPL-3.0" to readAsset(context, "licenses/KernelSU-GPL-3.0.txt") },
                    )
                    ArrowPreference(
                        title = "Miuix 与背景效果许可",
                        endActions = { Text("Apache-2.0", color = tokens.textSecondary) },
                        onClick = { message = "Miuix · Apache-2.0" to readAsset(context, "licenses/Miuix-Apache-2.0.txt") },
                    )
                }
            }
        }
    }
    message?.let { (title, text) -> InfoDialog(title, text) { message = null } }
}

private fun readAsset(context: Context, path: String): String = try {
    context.assets.open(path).bufferedReader().use { it.readText() }
} catch (_: Exception) {
    "许可文件读取失败：$path"
}
