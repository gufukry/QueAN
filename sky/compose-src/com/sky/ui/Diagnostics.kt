package com.sky.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Instant

internal fun shareDiagnostics(context: Context, version: String, state: SkyState) {
    val report = buildString {
        appendLine("sky diagnostics")
        appendLine("Time: ${Instant.now()}")
        appendLine("Version: $version")
        appendLine("Package: ${context.packageName}")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
        appendLine("Kernel: ${System.getProperty("os.version")}")
        appendLine("Page: ${state.page}")
        appendLine("Debug: ${state.debug}")
        appendLine("Theme: ${state.themeMode}")
        appendLine("Blur: ${state.blur}; Floating bar: ${state.floatingBar}; Liquid glass: ${state.liquidGlass}")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "sky $version 诊断日志")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "发送日志"))
}
