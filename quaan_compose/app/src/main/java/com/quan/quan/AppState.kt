package com.quan.quan

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

class AppState(ctx: Context) {
    private val app = ctx.applicationContext
    private val prefs = app.getSharedPreferences("quean", 0)

    var page by mutableStateOf(0)
    var dark by mutableStateOf(prefs.getBoolean("dark", false))
    var game by mutableStateOf(prefs.getInt("game", 0))          // 0=和平精英 1=PUBG
    var mode by mutableStateOf(prefs.getInt("mode", 0))          // 0=自动 1=普通 2=内核
    var online by mutableStateOf(false)
    var running by mutableStateOf(false)
    var log by mutableStateOf("")

    val gameNames = listOf("和平精英", "PUBG MOBILE")
    val gamePackages = listOf("com.tencent.tmgp.pubgmhd", "com.tencent.ig")

    fun setDark(v: Boolean) { dark = v; prefs.edit().putBoolean("dark", v).apply() }
    fun setGame(v: Int) { game = v; prefs.edit().putInt("game", v).apply() }
    fun setMode(v: Int) { mode = v; prefs.edit().putInt("mode", v).apply() }

    /** 执行 su 命令，返回输出 */
    fun sh(cmd: String): String = try {
        val p = ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().readText()
        p.waitFor()
        out.trim()
    } catch (t: Throwable) {
        "执行失败: ${t.message}"
    }

    fun refreshStatus() {
        val pid = sh("pidof quean_kernel.bin quean_kernel.sh heping heping_dfm | head -1")
        online = pid.isNotBlank() && !pid.startsWith("执行失败")
        running = online
    }

    /** 把内核从 assets 解出来并用 root 跑起来 */
    fun startKernel() {
        if (running) { appendLog("内核已在运行"); return }
        running = true
        appendLog("> 正在准备内核…")
        Thread {
            try {
                val asset = "loader/heping_dfm.sh"
                val bytes = app.assets.open(asset).readBytes()
                val f = File(app.filesDir, "quean_kernel.sh")
                f.writeBytes(bytes)
                appendLog("> 内核已释放（${bytes.size} 字节），开始执行")
                val out = sh("cp '${f.absolutePath}' /data/local/tmp/quean_kernel.sh && " +
                        "chmod 777 /data/local/tmp/quean_kernel.sh && " +
                        "cd /data/local/tmp && sh quean_kernel.sh < /dev/null")
                appendLog(out.ifBlank { "> 无输出" })
            } catch (t: Throwable) {
                appendLog("> 启动失败：${t.message}")
            } finally {
                refreshStatus()
                if (!online) running = false
            }
        }.start()
    }

    fun stopKernel() {
        Thread {
            appendLog("> 停止内核…")
            appendLog(sh("pkill -f quean_kernel; pkill -f heping; pkill -f heping_dfm"))
            refreshStatus()
        }.start()
    }

    private fun appendLog(s: String) {
        log = (log + s + "\n").takeLast(4000)
    }
}
