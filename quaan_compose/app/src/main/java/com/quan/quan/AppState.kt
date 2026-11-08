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
    var drv by mutableStateOf(prefs.getString("drv", "paradise") ?: "paradise")
    var drvSel by mutableStateOf(prefs.getString("drv_sel", "") ?: "")
    var drvList by mutableStateOf(listOf<String>())
    var drvState by mutableStateOf("未知")
    var kernelVer by mutableStateOf("")

    val gameNames = listOf("和平精英", "PUBG MOBILE")
    val gamePackages = listOf("com.tencent.tmgp.pubgmhd", "com.tencent.ig")

    fun updateDark(v: Boolean) { dark = v; prefs.edit().putBoolean("dark", v).apply() }
    fun updateGame(v: Int) { game = v; prefs.edit().putInt("game", v).apply() }
    fun updateMode(v: Int) { mode = v; prefs.edit().putInt("mode", v).apply() }

    fun updateDrv(v: String) {
        drv = v
        drvSel = ""
        prefs.edit().putString("drv", v).remove("drv_sel").apply()
        scanDrivers()
    }

    /** 通道下的驱动脚本列表 */
    fun scanDrivers() {
        val list = try {
            (app.assets.list("drivers/$drv") ?: emptyArray())
                .filter { it.contains(".") && !it.lowercase().contains("executemefirst") }
                .sorted()
        } catch (t: Throwable) {
            emptyList()
        }
        drvList = list
        if (drvSel.isBlank() || !list.contains(drvSel)) {
            drvSel = autoPick(list) ?: ""
        }
    }

    /** 按内核大版本挑脚本：6.12 → 6.12.ko.sh；没有就用最高的 */
    private fun autoPick(list: List<String>): String? {
        val kv = kernelVer.ifBlank { kvNow() }
        val parts = kv.split(".")
        val major = if (parts.size >= 2) parts[0] + "." + parts[1] else kv
        list.firstOrNull { it.startsWith(major) || it.contains(major) }?.let { return it }
        val numeric = list.filter { Regex("\\d+\\.\\d+").containsMatchIn(it) }
        val sorted = numeric.sortedByDescending {
            Regex("(\\d+)\\.(\\d+)").find(it)?.let { m ->
                (m.groupValues[1] + "." + m.groupValues[2]).toDoubleOrNull()
            } ?: 0.0
        }
        return sorted.firstOrNull() ?: list.firstOrNull()
    }

    fun kvNow(): String = sh("uname -r").takeWhile { it.isDigit() || it == '.' }.ifBlank { "-" }

    /** 驱动脚本连同目录一起放到 /data/local/qdrv 再执行 */
    fun runDriver() {
        if (drvSel.isBlank()) { appendLog("> 还没有选驱动"); return }
        appendLog("> 执行驱动：$drv / $drvSel")
        Thread {
            try {
                val dir = "/data/local/qdrv"
                val names = app.assets.list("drivers/$drv") ?: emptyArray()
                sh("rm -rf $dir && mkdir -p $dir && chmod 777 $dir")
                for (n in names) {
                    val bytes = app.assets.open("drivers/$drv/$n").readBytes()
                    val f = File(app.filesDir, "drv_$n")
                    f.writeBytes(bytes)
                    sh("cp '" + f.absolutePath + "' '$dir/$n' && chmod 777 '$dir/$n'")
                }
                val pre = if (drv == "twt") "sh Executemefirst.sh < /dev/null; " else ""
                val out = sh("cd $dir && $pre sh '$drvSel' < /dev/null")
                appendLog(if (out.isBlank()) "> 无输出" else out)
                val mods = sh("cat /proc/modules 2>/dev/null; lsmod 2>/dev/null")
                drvState = if (mods.contains("drv")) "已加载" else "已执行"
                appendLog("> 驱动状态：$drvState")
            } catch (t: Throwable) {
                appendLog("> 驱动执行失败：${t.message}")
            }
        }.start()
    }

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
