package com.quan.quan

import android.content.ClipData
import android.content.ClipboardManager
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
    // 内核云更新
    var noticeRaw by mutableStateOf("")
    var noticeText by mutableStateOf("")
    var upVer by mutableStateOf("")
    var upUrl by mutableStateOf("")
    var upNote by mutableStateOf("")
    var downloading by mutableStateOf(false)
    var progress by mutableStateOf(0f)
    var upStatus by mutableStateOf("尚未检查")
    // 卡密
    var kami by mutableStateOf(prefs.getString("kami", "") ?: "")
    var remember by mutableStateOf(prefs.getBoolean("remember", false))
    var licensed by mutableStateOf(prefs.getBoolean("licensed", false))
    var expireText by mutableStateOf(prefs.getString("kami_exp_text", "") ?: "")
    var logging by mutableStateOf(false)
    var loginMsg by mutableStateOf("")

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

    fun saveKami(v: String) {
        kami = v
        if (remember) prefs.edit().putString("kami", v).apply()
    }

    fun setRemember(v: Boolean) {
        remember = v
        prefs.edit().putBoolean("remember", v).apply()
        if (!v) prefs.edit().remove("kami").apply()
        else if (kami.isNotBlank()) prefs.edit().putString("kami", kami).apply()
    }

    /** 登录（后台线程，结果回主线程） */
    fun login() {
        if (kami.isBlank()) { loginMsg = "请输入卡密"; return }
        logging = true
        loginMsg = "正在验证…"
        Thread {
            val r = wyLogin(kami.trim())
            logging = false
            licensed = r.ok
            loginMsg = r.msg
            expireText = r.expireText
            prefs.edit().putBoolean("licensed", r.ok)
                .putString("kami_exp_text", r.expireText).apply()
            if (r.ok) {
                if (remember) prefs.edit().putString("kami", kami).apply()
                appendLog("> 卡密验证通过 ${r.expireText}")
            } else {
                appendLog("> 卡密验证失败：${r.msg}")
            }
        }.start()
    }

    fun logout() {
        licensed = false
        expireText = ""
        loginMsg = ""
        prefs.edit().putBoolean("licensed", false).remove("kami_exp_text").apply()
    }

    /** 本地配置文件（与 Java 版同名） */
    fun configFile(): File = File(app.filesDir, "android_config.json")

    fun exportConfig(): String {
        val json = """
            {
              "game": $game,
              "mode": $mode,
              "drv": "$drv",
              "drv_sel": "$drvSel",
              "kernel_ver": "${localKernelVer()}",
              "exported_at": "${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA).format(java.util.Date())}"
            }
        """.trimIndent()
        return try {
            configFile().writeText(json)
            "已导出到 ${configFile().absolutePath}"
        } catch (t: Throwable) {
            "导出失败：${t.message}"
        }
    }

    fun importConfig(text: String?): String {
        if (text.isNullOrBlank()) return "没有读取到内容"
        return try {
            Regex(""" "game"\s*:\s*(\d+) """.trim()).find(text)
                ?.let { updateGame(it.groupValues[1].toInt()) }
            Regex(""" "mode"\s*:\s*(\d+) """.trim()).find(text)
                ?.let { updateMode(it.groupValues[1].toInt()) }
            Regex(""" "drv"\s*:\s*"([^"]*)" """.trim()).find(text)
                ?.let { updateDrv(it.groupValues[1]) }
            configFile().writeText(text)
            "导入完成"
        } catch (t: Throwable) {
            "导入失败：${t.message}"
        }
    }

    /** 本地内核文件（按游戏分开） */
    fun kernelFile(): File = File(app.filesDir, "quean_kernel_${gameKey()}.sh")
    fun kernelVerFile(): File = File(app.filesDir, "quean_kernel_${gameKey()}.ver")
    fun gameKey(): String = if (game == 1) "pubg" else "hp"
    fun localKernelVer(): String {
        val f = kernelVerFile()
        if (f.exists() && f.length() > 0) return f.readText().trim()
        return prefs.getString("kernel_ver_${gameKey()}", "") ?: ""
    }
    fun hasKernel(): Boolean = kernelFile().length() > 1024

    /** 拉公告 → 解析当前游戏的版本/直链 */
    fun checkUpdate() {
        upStatus = "正在检查…"
        Thread {
            val raw = fetchNotice()
            if (raw == null) {
                upStatus = "公告获取失败，可直接点更新"
                return@Thread
            }
            noticeRaw = raw
            noticeText = stripUpdateLines(raw)
            applyUpdateFor(raw)
            val local = localKernelVer()
            upStatus = when {
                upUrl.isBlank() -> "这一份内核没有配置下载地址"
                local.isBlank() -> "还没有下载过内核，可以直接更新"
                compareVersion(upVer, local) > 0 -> "有新版 v$upVer（本地 v$local）"
                else -> "已是最新 v$local"
            }
        }.start()
    }

    private fun applyUpdateFor(raw: String) {
        val block = updateBlock(raw, if (game == 0) "" else gameKey())
            ?: updateBlock(raw, "")
        if (block == null) { upVer = ""; upUrl = ""; return }
        val bar1 = block.indexOf('|')
        if (bar1 < 0) {
            upVer = block.trim(); upUrl = ""; upNote = ""
        } else {
            upVer = block.substring(0, bar1).trim()
            val bar2 = block.indexOf('|', bar1 + 1)
            val rest = if (bar2 < 0) block.substring(bar1 + 1) else block.substring(bar1 + 1, bar2)
            upUrl = rest.lineSequence().firstOrNull()?.trim()?.removeSuffix("#") ?: ""
            upNote = if (bar2 < 0) "" else block.substring(bar2 + 1).trim().replace("\\n", "\n")
        }
        if (!upUrl.startsWith("http")) upUrl = ""
    }

    /** 下载当前游戏的内核 */
    fun startUpdate() {
        if (downloading) return
        if (upUrl.isBlank()) { upStatus = "下载地址为空"; return }
        downloading = true
        progress = 0f
        upStatus = "正在下载…"
        Thread {
            val ok = downloadTo(upUrl, kernelFile()) { got, total ->
                progress = if (total > 0) got.toFloat() / total else 0f
            }
            if (ok) {
                kernelVerFile().writeText(upVer)
                prefs.edit().putString("kernel_ver_${gameKey()}", upVer).apply()
                upStatus = "已更新到 v$upVer"
            } else {
                upStatus = "下载失败，请重试"
            }
            downloading = false
        }.start()
    }

    /** 复制设备信息到剪贴板 */
    fun copyDevice() {
        val text = "机型: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n" +
                "Android ${android.os.Build.VERSION.RELEASE} · API ${android.os.Build.VERSION.SDK_INT}\n" +
                "内核: ${kernelVer}\n" +
                "核心: ${if (online) "运行中" else "未运行"}"
        try {
            val cm = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("device", text))
            appendLog("> 已复制设备信息")
        } catch (t: Throwable) {
            appendLog("> 复制失败：${t.message}")
        }
    }

    /** 读剪贴板文本（导入配置用） */
    fun readClipboard(): String = try {
        val cm = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.primaryClip?.getItemAt(0)?.coerceToText(app)?.toString() ?: ""
    } catch (t: Throwable) {
        ""
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
                val f = File(app.filesDir, "quean_kernel.sh")
                if (kernelFile().length() > 1024) {
                    kernelFile().copyTo(f, overwrite = true)   // 云更新的内核优先
                    appendLog("> 使用云端内核 v" + localKernelVer())
                } else {
                    val asset = "loader/heping_dfm.sh"
                    val bytes = app.assets.open(asset).readBytes()
                    f.writeBytes(bytes)
                }
                val bytes = f.readBytes()
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
