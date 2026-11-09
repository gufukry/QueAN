package com.sky.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.overlay.OverlayDialog

@Composable
internal fun DriverPicker(state: SkyState, close: () -> Unit) {
    val downloads by state.downloadRepository.downloads.collectAsState()
    val entries = state.compatibleDrivers()
    OverlayDialog(show = true, title = "驱动下载", onDismissRequest = close) {
        Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(state.configStatus, color = SkyTheme.colors.textSecondary)
            if (entries.isEmpty()) Text("暂无适配的驱动，请先登录并配置远程驱动列表。")
            entries.forEach { entry ->
                Text(entry.name)
                val task = downloads.firstOrNull { it.entry.id == entry.id }
                Text(task?.let { "${statusName(it.status)} · ${it.downloadedBytes / 1024} / ${entry.size / 1024} KiB" } ?: "未下载")
                if (task?.status in listOf(DriverDownloadStatus.DOWNLOADING, DriverDownloadStatus.QUEUED, DriverDownloadStatus.VERIFYING)) Action("取消") { state.cancelDownload(entry.id) }
                else if (task?.status == DriverDownloadStatus.COMPLETED) {
                    Text(if (task.integrity == DriverIntegrity.SHA256_VERIFIED) "SHA-256 校验通过" else "仅大小校验通过，未配置 SHA-256", color = SkyTheme.colors.textSecondary)
                    Action("删除文件") { state.deleteDownload(entry.id) }
                } else Action(if (task == null) "下载" else "重试") { state.download(entry) }
            }
            downloads.filter { saved -> entries.none { it.id == saved.entry.id } }.forEach { saved ->
                Text("${saved.entry.name} · ${statusName(saved.status)}")
                Action("删除本地记录与文件") { state.deleteDownload(saved.entry.id) }
            }
            state.downloadError?.let { Text(it, color = SkyTheme.colors.error) }
            top.yukonga.miuix.kmp.basic.TextButton(text = "关闭", onClick = close)
        }
    }
}
private fun statusName(status: DriverDownloadStatus) = when (status) {
    DriverDownloadStatus.QUEUED -> "等待中"
    DriverDownloadStatus.DOWNLOADING -> "下载中"
    DriverDownloadStatus.VERIFYING -> "校验中"
    DriverDownloadStatus.COMPLETED -> "已下载"
    DriverDownloadStatus.CANCELLED -> "已取消"
    DriverDownloadStatus.FAILED -> "失败"
}
