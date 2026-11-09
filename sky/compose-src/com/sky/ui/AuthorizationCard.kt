package com.sky.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.TextField

@Composable
internal fun AuthorizationCard(state: SkyState) {
    Panel("授权") {
        val auth = state.auth
        if (auth == null) {
            TextField(value = state.cardInput, onValueChange = { state.cardInput = it }, label = "输入卡密", singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Toggle("记住卡密", "加密保存在本机，重启后重新验证", state.rememberCard) { state.rememberCard = it }
            top.yukonga.miuix.kmp.basic.Button(onClick = state::login, enabled = !state.authBusy && state.cardInput.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text(if (state.authBusy) "验证中…" else "登录") }
            Text(state.authStatus, color = SkyTheme.colors.textSecondary)
        } else {
            Text(state.authStatus, color = if (state.authVerified) SkyTheme.colors.success else SkyTheme.colors.warning)
            Text("有效期：" + if (auth.durationType == "free") "后台免费模式" else if (auth.expiresAt > 0) java.time.Instant.ofEpochSecond(auth.expiresAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().toString() else "长期")
            if (state.secondsRemaining in 1..259200) Text("授权将在 ${state.secondsRemaining / 3600} 小时内到期", color = SkyTheme.colors.warning)
            Text("类型：${auth.cardType.ifBlank { "未返回" }} · ${auth.durationType.ifBlank { "未返回" }}")
            if (auth.note.isNotBlank()) Text(auth.note)
            Text(state.heartbeatStatus, color = SkyTheme.colors.textSecondary)
            Action("切换卡密／退出授权", state::logout)
        }
    }
}
