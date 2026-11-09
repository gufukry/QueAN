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
    var game by mutableStateOf(prefs.getInt("game", 0))
    var mode by mutableStateOf(prefs.getInt("mode", 0))
    var online by mutableStateOf(false)
    var hasRoot by mutableStateOf(false)
    var failed by mutableStateOf(false)
    var running by mutableStateOf(false)
    var log by mutableStateOf("")
    var drv by mutableStateOf(
        (prefs.getString("drv", "paradise") ?: "paradise").ifBlank { "paradise" }
    )
    var drvSel by mutableStateOf(prefs.getString("drv_sel", "") ?: "")
    var drvList by mutableStateOf(listOf<String>())
    var drvState by mutableStateOf("未知")
    var kernelVer by mutableStateOf("")
    // 云更新
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
    var needKernel by mutableStateOf(false)
    var showKernelPrompt by mutableStateOf(false)
    var upGame by mutableStateOf(0)          // 更新页选中的游戏
    var showUpdatePage by mutableStateOf(false)
    var updatePrompt by mutableStateOf(false)
    var promptGame by mutableStateOf(0)
    var promptVer by mutableStateOf("")
    var cfgInput by mutableStateOf("")
    var cfgStatus by mutableStateOf("")

    val gameNames = listOf("和平精英", "PUBG MOBILE")

    /** 设备码（微验的 markcode） */
    fun deviceId(): String {
        val id = android.provider.Settings.Secure.getString(
            app.contentResolver, android.provider.Settings.Secure.ANDROID_ID
        )
        return if (id.isNullOrBlank()) "unknown_device" else id
    }

    fun updateDark(v: Boolean) { dark = v; prefs.edit().putBoolean("dark", v).apply() }
    fun updateGame(v: Int) { game = v; prefs.edit().putInt("game", v).apply() }
    fun updateMode(v: Int) { mode = v; prefs.edit().putInt("mode", v).apply() }

    fun updateDrv(v: String) {
        drv = v
        drvSel = ""
        prefs.edit().putString("drv", v).remove("drv_sel").apply()
        scanDrivers()
    }

    fun shCode(cmd: String): Pair<String, Int> {
        return try {
            val p = ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start()
            val out: String = p.inputStream.bufferedReader().readText()
            p.waitFor()
            Pair(out, p.exitValue())
        } catch (t: Throwable) {
            Pair("执行失败: " + t.message, -1)
        }
    }

    /** 长驻进程：边跑边把输出塞进日志 */
    private fun shStream(cmd: String) {
        try {
            val p = ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start()
            val r = p.inputStream.bufferedReader()
            while (true) {
                val line = r.readLine() ?: break
                if (line.isNotBlank()) appendLog(line)
            }
            p.waitFor()
            appendLog("> 内核进程退出（码 ${p.exitValue()}）")
        } catch (t: Throwable) {
            appendLog("> 运行失败：${t.message}")
            failed = true
        }
    }

    private fun isElf(f: File): Boolean = try {
        val h = f.inputStream().use { it.readNBytes(4) }
        h.size == 4 && h[0].toInt() == 0x7F &&
                h[1].toInt() == 'E'.code && h[2].toInt() == 'L'.code && h[3].toInt() == 'F'.code
    } catch (t: Throwable) {
        false
    }

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
        checkRoot()
    }

    /** 是否拿到 root：有 root 就视为"已成功" */
    fun checkRoot() {
        val out = sh("id")
        hasRoot = out.contains("uid=0")
    }

    /** 顶部状态：有 root 且没失败过 = 已成功，否则需检查 */
    fun statusOk(): Boolean = hasRoot && !failed

    fun statusText(): String = if (statusOk()) "已成功" else "需检查"

    /* ---------------- 卡密 ---------------- */

    fun saveKami(v: String) {
        kami = v
        if (remember) prefs.edit().putString("kami", v).apply()
    }

    fun updateRemember(v: Boolean) {
        remember = v
        prefs.edit().putBoolean("remember", v).apply()
        if (!v) prefs.edit().remove("kami").apply()
        else if (kami.isNotBlank()) prefs.edit().putString("kami", kami).apply()
    }

    /** 首次打开检查：没内核就弹提示 */
    /** 导入内核配置：纯数字走服务器取码，HXCFG1. 开头直接解析 */
    fun importKernelCfg() {
        val input = cfgInput.trim()
        if (input.isEmpty()) { cfgStatus = "请先输入配置码或数字码"; return }
        cfgStatus = "正在处理…"
        Thread {
            var code = input
            if (input.all { it.isDigit() }) {
                val fetched = KernelCfg.fetchByNumber(input)
                if (fetched == null) {
                    cfgStatus = "配置码不存在或服务器无返回"
                    return@Thread
                }
                code = fetched
            }
            val (cfg, err) = KernelCfg.decode(code)
            if (cfg == null) {
                cfgStatus = err ?: "解析失败"
                return@Thread
            }
            val ok = KernelCfg.write(cfg, app.filesDir) { cmd -> sh(cmd) }
            cfgStatus = if (ok) "配置已导入，重启内核后生效" else "写入失败（需要 root）"
            if (ok) {
                File(app.filesDir, "last_cfg_code.txt").writeText(code)
                appendLog("> 内核配置已导入")
            }
        }.start()
    }

    fun checkFirstRun() {
        upGame = game
        val downloaded = prefs.getBoolean("first_dl_" + gameKey(), false)
        needKernel = !hasKernel()
        showKernelPrompt = needKernel && !downloaded
    }

    fun dismissKernelPrompt() {
        showKernelPrompt = false
    }

    /** 刷驱动：默认输入 1（insmod），失败自动换 2（ksud） */
    private fun runDriverBlocking(): Boolean {
        val hasParadise = try {
            (app.assets.list("drivers/paradise") ?: emptyArray()).isNotEmpty()
        } catch (t: Throwable) {
            false
        }
        if (hasParadise && drv != "paradise") {
            drv = "paradise"
            drvSel = ""
            prefs.edit().putString("drv", "paradise").remove("drv_sel").apply()
        }
        if (drvSel.isBlank()) scanDrivers()
        if (drvSel.isBlank()) {
            appendLog("> 没有找到可用驱动，跳过驱动步骤")
            return false
        }
        appendLog("> ① 自动导入驱动：$drv / $drvSel")
        return try {
            val names = app.assets.list("drivers/$drv") ?: emptyArray()
            val tmp = File(app.filesDir, "qdrv").also { it.mkdirs() }
            for (n in names) {
                if (!n.contains(".")) continue
                File(tmp, n).writeBytes(app.assets.open("drivers/$drv/$n").readBytes())
            }
            val inFile = File(app.filesDir, "user_driver.in").also { it.writeText("1") }
            val pre = if (drv == "twt")
                names.filter { it.lowercase().contains("executemefirst") }
                    .joinToString("") { "sh '$it' < /dev/null; " } else ""
            fun build(input: String) = "rm -rf /data/local/qdrv && mkdir -p /data/local/qdrv && " +
                    "cp -f '${tmp.absolutePath}'/* /data/local/qdrv/ && " +
                    "chmod -R 777 /data/local/qdrv && " +
                    "cp '${inFile.absolutePath}' /data/local/tmp/quean_in.txt && " +
                    "cd /data/local/qdrv && $pre sh '$drvSel' '$input' " +
                    "< /data/local/tmp/quean_in.txt"

            val (out1, code1) = shCode(build("1"))
            if (out1.isNotBlank()) appendLog(out1)
            var ok = code1 == 0
            if (!ok) {
                appendLog("> insmod 方式失败（退出码 $code1），改用 ksud 重试")
                val (out2, code2) = shCode(build("2"))
                if (out2.isNotBlank()) appendLog(out2)
                ok = code2 == 0
            }
            val mods = sh("cat /proc/modules 2>/dev/null; lsmod 2>/dev/null")
            drvState = if (mods.contains("drv")) "已加载" else if (ok) "已执行" else "失败"
            if (!ok) failed = true
            appendLog("> 驱动状态：$drvState")
            ok
        } catch (t: Throwable) {
            appendLog("> 驱动执行失败：${t.message}")
            false
        }
    }

    /** 登录 → 驱动 → 内核（悬浮窗由内核自己弹，不启动游戏） */
    fun startFlow() {
        if (logging) return
        failed = false
        if (kami.isBlank()) { loginMsg = "请输入卡密"; return }
        logging = true
        loginMsg = "正在验证…"
        Thread {
            val r = wyLogin(kami.trim(), deviceId())
            logging = false
            licensed = r.ok
            loginMsg = r.msg
            expireText = r.expireText
            prefs.edit().putBoolean("licensed", r.ok)
                .putString("kami_exp_text", r.expireText).apply()
            if (!r.ok) {
                appendLog("> 卡密验证失败：${r.msg}")
                return@Thread
            }
            if (remember) prefs.edit().putString("kami", kami).apply()
            appendLog("> 卡密验证通过 ${r.expireText}")
            runDriverBlocking()
            appendLog("> ② 正在启动内核…")
            startKernel()
        }.start()
    }

    fun login() {
        if (kami.isBlank()) { loginMsg = "请输入卡密"; return }
        logging = true
        loginMsg = "正在验证…"
        Thread {
            val r = wyLogin(kami.trim(), deviceId())
            logging = false
            licensed = r.ok
            loginMsg = r.msg
            expireText = r.expireText
            prefs.edit().putBoolean("licensed", r.ok)
                .putString("kami_exp_text", r.expireText).apply()
            if (r.ok) {
                if (remember) prefs.edit().putString("kami", kami).apply()
                appendLog("> 卡密验证通过 ${r.expireText}")
                startKernel()
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

    /* ---------------- 驱动 ---------------- */

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

    private fun autoPick(list: List<String>): String? {
        val kv = kernelVer.ifBlank { kvNow() }
        val parts = kv.split(".")
        val major = if (parts.size >= 2) parts[0] + "." + parts[1] else kv
        list.firstOrNull { it.startsWith(major) || it.contains(major) }?.let { return it }
        return list.firstOrNull()
    }

    /** 本机内核版本：优先系统属性（无需 root），再用 uname 兜底 */
    fun kvNow(): String {
        val prop = System.getProperty("os.version") ?: ""
        if (prop.isNotBlank()) return prop
        val u = sh("uname -r")
        if (u.startsWith("执行失败")) return "-"
        return u.ifBlank { "-" }
    }

    // 终端
    var showTerminal by mutableStateOf(false)
    var termLog by mutableStateOf("")
    var termInput by mutableStateOf("")
    var termRunning by mutableStateOf(false)
    private var termCwd = "/data/local/tmp"

    var driverDialog by mutableStateOf(false)
    var driverInput by mutableStateOf("1")
    var driverLog by mutableStateOf("")

    fun openDriverDialog() {
        driverLog = ""
        driverDialog = true
    }

    fun closeDriverDialog() {
        driverDialog = false
    }

    private fun execStream(cmd: String, sink: (String) -> Unit): Int = try {
        val p = ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start()
        val r = p.inputStream.bufferedReader()
        while (true) {
            val line = r.readLine() ?: break
            if (line.isNotBlank()) sink(line)
        }
        p.waitFor()
        p.exitValue()
    } catch (t: Throwable) {
        sink("执行失败：" + t.message)
        -1
    }

    /** 手动刷驱动：输入框里的内容（默认 1）同时作为 argv 和 stdin */
    fun runDriverManual() {
        if (drvSel.isBlank()) {
            driverLog = "还没有选驱动"
            return
        }
        val input = driverInput.trim().ifBlank { "1" }
        driverLog = "> 驱动：$drv / $drvSel   输入：$input\n"
        Thread {
            try {
                val names = app.assets.list("drivers/$drv") ?: emptyArray()
                val tmp = File(app.filesDir, "qdrv").also { it.mkdirs() }
                for (n in names) {
                    if (!n.contains(".")) continue
                    File(tmp, n).writeBytes(app.assets.open("drivers/$drv/$n").readBytes())
                }
                val inFile = File(app.filesDir, "user_driver.in").also { it.writeText(input) }
                val pre = if (drv == "twt")
                    names.filter { it.lowercase().contains("executemefirst") }
                        .joinToString("") { "sh '$it' < /dev/null; " } else ""
                val cmd = "rm -rf /data/local/qdrv && mkdir -p /data/local/qdrv && " +
                        "cp -f '${tmp.absolutePath}'/* /data/local/qdrv/ && " +
                        "chmod -R 777 /data/local/qdrv && " +
                        "cp '${inFile.absolutePath}' /data/local/tmp/quean_in.txt && " +
                        "cd /data/local/qdrv && $pre sh '$drvSel' '$input' " +
                        "< /data/local/tmp/quean_in.txt"
                val code = execStream(cmd) { line ->
                    driverLog = (driverLog + line + "\n").takeLast(4000)
                    appendLog(line)
                }
                driverLog += "> 退出码 $code\n"
                val mods = sh("cat /proc/modules 2>/dev/null; lsmod 2>/dev/null")
                drvState = if (mods.contains("drv")) "已加载" else if (code == 0) "已执行" else "失败"
                driverLog += "> 驱动状态：$drvState\n"
                appendLog("> 驱动状态：$drvState")
                if (code != 0) failed = true
            } catch (t: Throwable) {
                driverLog += "> 失败：${t.message}\n"
            }
        }.start()
    }

    fun openTerminal() {
        showTerminal = true
        if (termLog.isBlank()) termLog = "> QueAN 终端（root）\n> 输入命令后回车执行，支持 cd / sh xxx.sh\n"
    }

    fun closeTerminal() {
        showTerminal = false
    }

    fun clearTerminal() {
        termLog = ""
    }

    /** 在 root shell 里执行一行命令，输出实时进终端 */
    fun runTerm() {
        val raw = termInput.trim()
        if (raw.isEmpty() || termRunning) return
        termInput = ""
        termRunning = true
        termLog += "\n$ $raw\n"
        if (raw.startsWith("cd ")) {
            val target = raw.substring(3).trim()
            Thread {
                val (out, _) = shCode("cd $termCwd && cd $target 2>&1 && pwd")
                val dir = out.lineSequence().lastOrNull { it.startsWith("/") }
                if (dir != null) termCwd = dir.trim()
                termLog += if (dir != null) "" else out
                termRunning = false
            }.start()
            return
        }
        Thread {
            val code = execStream("cd $termCwd 2>/dev/null; $raw") { line ->
                termLog += line + "\n"
            }
            if (code != 0) termLog += "[退出码 $code]\n"
            termRunning = false
        }.start()
    }

    /** 选一个 .sh 拷进来直接跑 */
    fun runScript(uri: android.net.Uri) {
        termRunning = true
        termLog += "\n> 导入脚本并执行\n"
        Thread {
            try {
                val f = File(app.filesDir, "term_script.sh")
                app.contentResolver.openInputStream(uri)?.use { input ->
                    f.outputStream().use { input.copyTo(it) }
                }
                termLog += "> 已保存到 ${f.absolutePath}\n"
                val code = execStream("cp '${f.absolutePath}' /data/local/tmp/term_script.sh && chmod 777 /data/local/tmp/term_script.sh && cd $termCwd && sh /data/local/tmp/term_script.sh 1") { line ->
                    termLog += line + "\n"
                }
                termLog += "[退出码 $code]\n"
            } catch (t: Throwable) {
                termLog += "> 执行失败：${t.message}\n"
            }
            termRunning = false
        }.start()
    }

    fun runDriver() {
        if (drvSel.isBlank()) { appendLog("> 还没有选驱动"); return }
        appendLog("> 手动刷驱动：$drv / $drvSel")
        Thread { runDriverBlocking() }.start()
    }

    /* ---------------- 内核 ---------------- */

    fun gameKey(g: Int = game): String = if (g == 1) "pubg" else "hp"
    fun kernelFile(g: Int = game): File = File(app.filesDir, "quean_kernel_${gameKey(g)}.sh")
    fun kernelVerFile(g: Int = game): File = File(app.filesDir, "quean_kernel_${gameKey(g)}.ver")

    fun localKernelVer(g: Int = game): String {
        val f = kernelVerFile(g)
        if (f.exists() && f.length() > 0) return f.readText().trim()
        return prefs.getString("kernel_ver_${gameKey(g)}", "") ?: ""
    }

    fun hasKernel(g: Int = game): Boolean = kernelFile(g).length() > 1024

    fun startKernel() {
        if (running) {
            appendLog("> 内核已在运行")
            return
        }
        val src = kernelFile(game)
        if (src.length() < 1024) {
            appendLog("> 还没有内核文件，先在配置页「检查内核更新」下载")
            running = false
            failed = true
            return
        }
        running = true
        Thread {
            val elf = isElf(src)
            val target = if (elf) "quean_kernel.bin" else "quean_kernel.sh"
            val run = if (elf) "./$target" else "sh $target"
            val cmd = "cp '${src.absolutePath}' /data/local/tmp/$target && " +
                    "chmod 777 /data/local/tmp/$target && cd /data/local/tmp && " +
                    "HX_DRV=$drv HX_NORECORD=false $run < /dev/null"
            appendLog("> 运行内核（${if (elf) "ELF" else "脚本"}）：$target  驱动=$drv")
            shStream(cmd)
            refreshStatus()
            running = false
        }.start()
    }

    fun stopKernel() {
        Thread {
            appendLog("> 停止内核…")
            appendLog(sh("pkill -f /data/local/tmp/quean_kernel; pkill -f heping_dfm"))
            refreshStatus()
        }.start()
    }

    fun copyDevice() {
        val text = "机型: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n" +
                "Android ${android.os.Build.VERSION.RELEASE} · API ${android.os.Build.VERSION.SDK_INT}\n" +
                "内核: $kernelVer\n核心: ${if (online) "运行中" else "未运行"}"
        try {
            val cm = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("device", text))
            appendLog("> 已复制设备信息")
        } catch (t: Throwable) {
            appendLog("> 复制失败：${t.message}")
        }
    }

    fun readClipboard(): String = try {
        val cm = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.primaryClip?.getItemAt(0)?.coerceToText(app)?.toString() ?: ""
    } catch (t: Throwable) {
        ""
    }

    /* ---------------- 配置 ---------------- */

    fun configFile(): File = File(app.filesDir, "android_config.json")

    fun exportConfig(): String = try {
        val json = "{\n" +
                "  \"game\": $game,\n" +
                "  \"mode\": $mode,\n" +
                "  \"drv\": \"$drv\",\n" +
                "  \"drv_sel\": \"$drvSel\",\n" +
                "  \"kernel_ver\": \"${localKernelVer()}\"\n" +
                "}"
        configFile().writeText(json)
        appendLog("> 配置已导出：${configFile().absolutePath}")
        "已导出到 ${configFile().absolutePath}"
    } catch (t: Throwable) {
        "导出失败：${t.message}"
    }

    fun importConfig(text: String?): String {
        if (text.isNullOrBlank()) return "剪贴板里没有内容"
        return try {
            pickInt(text, "game")?.let { updateGame(it) }
            pickInt(text, "mode")?.let { updateMode(it) }
            pickString(text, "drv")?.let { updateDrv(it) }
            configFile().writeText(text)
            appendLog("> 配置已导入")
            "导入完成"
        } catch (t: Throwable) {
            "导入失败：${t.message}"
        }
    }

    private fun pickInt(text: String, key: String): Int? {
        val k = "\"" + key + "\""
        var i = text.indexOf(k)
        if (i < 0) return null
        i = text.indexOf(':', i + k.length)
        if (i < 0) return null
        val sb = StringBuilder()
        for (j in i + 1 until text.length) {
            val c = text[j]
            if (c.isDigit() || c == '-') sb.append(c) else if (sb.isNotEmpty()) break else if (c != ' ') break
        }
        return sb.toString().toIntOrNull()
    }

    private fun pickString(text: String, key: String): String? {
        val k = "\"" + key + "\""
        var i = text.indexOf(k)
        if (i < 0) return null
        i = text.indexOf(':', i + k.length)
        if (i < 0) return null
        val start = text.indexOf('"', i)
        if (start < 0) return null
        val end = text.indexOf('"', start + 1)
        if (end < 0) return null
        return text.substring(start + 1, end)
    }

    /* ---------------- 云更新 ---------------- */

    fun checkUpdate() {
        upStatus = "正在检查…"
        Thread {
            val raw = fetchNotice()
            if (raw == null) {
                upStatus = "公告获取失败"
                return@Thread
            }
            noticeRaw = raw
            noticeText = stripUpdateLines(raw)
            applyUpdateFor(raw)
            val local = localKernelVer(upGame)
            upStatus = when {
                upUrl.isBlank() -> "这一份内核没有配置下载地址"
                local.isBlank() -> "还没有下载过内核"
                compareVersion(upVer, local) > 0 -> "有新版 v$upVer（本地 v$local）"
                else -> "已是最新 v$local"
            }
        }.start()
    }

    /** 检查当前更新页所选游戏：有新版就进更新页，最新就只提示 */
    fun checkAndRoute() {
        upStatus = "正在检查…"
        Thread {
            val raw = fetchNotice()
            if (raw == null) {
                toast("公告获取失败，请检查网络")
                upStatus = "公告获取失败"
                return@Thread
            }
            noticeRaw = raw
            noticeText = stripUpdateLines(raw)
            applyUpdateFor(raw)
            val local = localKernelVer(upGame)
            val newer = !hasKernel(upGame) ||
                    (upVer.isNotBlank() && (local.isBlank() || compareVersion(upVer, local) > 0))
            if (newer) {
                upStatus = if (local.isBlank()) "还没有下载过内核" else "有新版 v$upVer（本地 v$local）"
                showUpdatePage = true
            } else {
                upStatus = "已是最新 v$local"
                toast("已是最新版本 v$local")
            }
        }.start()
    }

    /** 启动时静默检查当前游戏：有新版就弹提示（不跳页） */
    fun autoCheckUpdate() {
        if (showKernelPrompt || needKernel) return   // 首次使用只提示下载内核
        Thread {
            val raw = fetchNotice() ?: return@Thread
            noticeRaw = raw
            noticeText = stripUpdateLines(raw)
            applyUpdateFor(raw, game)
            val local = localKernelVer(game)
            val newer = !hasKernel(game) ||
                    (upVer.isNotBlank() && (local.isBlank() || compareVersion(upVer, local) > 0))
            if (newer && upUrl.isNotBlank()) {
                promptGame = game
                promptVer = upVer
                updatePrompt = true
            }
        }.start()
    }

    /** 弹窗里点「前往更新」：打开更新页并选好对应游戏 */
    fun gotoUpdateFromPrompt() {
        upGame = promptGame
        updatePrompt = false
        openUpdatePage(promptGame)
    }

    fun dismissUpdatePrompt() {
        updatePrompt = false
    }

    fun openUpdatePage(g: Int) {
        upGame = g
        applyUpdateFor(noticeRaw)
        showUpdatePage = true
    }

    fun closeUpdatePage() {
        showUpdatePage = false
    }

    private fun toast(msg: String) {
        try {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(app, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (t: Throwable) {
            // ignore
        }
    }

    private fun applyUpdateFor(raw: String, g: Int = upGame) {
        val block = updateBlock(raw, if (g == 0) "" else gameKey(g))
            ?: updateBlock(raw, "")
        if (block == null) { upVer = ""; upUrl = ""; return }
        val bar1 = block.indexOf('|')
        if (bar1 < 0) {
            upVer = block.trim()
            upUrl = ""
            upNote = ""
        } else {
            upVer = block.substring(0, bar1).trim()
            val bar2 = block.indexOf('|', bar1 + 1)
            val urlPart = if (bar2 < 0) block.substring(bar1 + 1) else block.substring(bar1 + 1, bar2)
            upUrl = urlPart.lineSequence().firstOrNull()?.trim()?.removeSuffix("#") ?: ""
            upNote = if (bar2 < 0) "" else block.substring(bar2 + 1).trim().replace("\\n", "\n")
        }
        if (!upUrl.startsWith("http")) upUrl = ""
    }

    fun startUpdate() {
        if (downloading) return
        if (upUrl.isBlank()) { upStatus = "下载地址为空"; return }
        downloading = true
        progress = 0f
        upStatus = "正在下载…"
        Thread {
            val ok = downloadTo(upUrl, kernelFile(upGame)) { got, total ->
                progress = if (total > 0) got.toFloat() / total else 0f
            }
            if (ok) {
                kernelVerFile(upGame).writeText(upVer)
                prefs.edit().putString("kernel_ver_${gameKey(upGame)}", upVer)
                    .putBoolean("first_dl_${gameKey(upGame)}", true).apply()
                needKernel = false
                upStatus = "已更新到 v$upVer"
                Thread.sleep(600)
                showUpdatePage = false
                appendLog("> 内核已更新到 v$upVer")
            } else {
                upStatus = "下载失败，请重试"
            }
            downloading = false
        }.start()
    }

    fun clearLog() {
        log = ""
    }

    private fun appendLog(s: String) {
        log = (log + s + "\n").takeLast(4000)
    }
}
