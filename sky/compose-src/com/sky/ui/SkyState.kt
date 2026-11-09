package com.sky.ui

import android.app.Application
import android.net.Uri
import java.io.File
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.os.Build
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SkyState(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("settings", 0)
    var themeMode by mutableStateOf(preferences.getString("theme_mode", if (preferences.getBoolean("dark", true)) "dark" else "light") ?: "dark")
        private set
    var startupComplete by mutableStateOf(false)
        private set
    fun finishStartup() { startupComplete = true }
    var blur by mutableStateOf(preferences.getBoolean("blur", true)); private set
    var floatingBar by mutableStateOf(preferences.getBoolean("floating_bar", true)); private set
    var liquidGlass by mutableStateOf(preferences.getBoolean("liquid_glass", true)); private set
    var predictiveBack by mutableStateOf(preferences.getBoolean("predictive_back", true)); private set
    var swipeBack by mutableStateOf(preferences.getBoolean("swipe_back", true)); private set
    var pageGesture by mutableStateOf(preferences.getString("page_gesture", "cross") ?: "cross"); private set
    var scale by mutableIntStateOf(preferences.getInt("ui_scale", 100)); private set
    var debug by mutableStateOf(DebugOptions(
        squircle = preferences.getBoolean("debug_squircle", true),
        haptic = preferences.getBoolean("debug_haptic", true),
        userScroll = preferences.getBoolean("debug_user_scroll", true),
        topBar = preferences.getBoolean("debug_top_bar", true),
        smallTopBar = preferences.getBoolean("debug_small_top_bar", false),
        navigation = preferences.getBoolean("debug_navigation", true),
        badge = preferences.getBoolean("debug_badge", false),
        toolbar = preferences.getBoolean("debug_toolbar", false),
        fab = preferences.getBoolean("debug_fab", false),
        cornerClip = preferences.getBoolean("debug_corner_clip", true),
        dim = preferences.getBoolean("debug_dim", true),
        blockInput = preferences.getBoolean("debug_block_input", true),
        navigationMode = preferences.getInt("debug_navigation_mode", 0),
        transition = preferences.getInt("debug_transition", 0),
    )); private set
    private val authorization = AuthorizationRepository(application)
    val downloadRepository = DriverDownloadRepository(application)
    internal val program = ProgramController(viewModelScope)
    var programArguments by mutableStateOf("")
    var cardInput by mutableStateOf("")
    var rememberCard by mutableStateOf(false)
    internal var auth by mutableStateOf<Authorization?>(null); private set
    var authStatus by mutableStateOf("未登录"); private set
    var authBusy by mutableStateOf(false); private set
    var authVerified by mutableStateOf(false); private set
    var secondsRemaining by mutableStateOf(0L); private set
    var heartbeatStatus by mutableStateOf("未登录"); private set
    var remoteConfig by mutableStateOf(SkyRemoteConfig()); private set
    var configStatus by mutableStateOf("登录后加载远程配置"); private set
    internal var updateInfo by mutableStateOf<AppUpdate?>(null); private set
    var updateStatus by mutableStateOf("尚未检查更新"); private set
    var updateBusy by mutableStateOf(false); private set
    var downloadError by mutableStateOf<String?>(null); private set
    internal val updateRequired: Boolean get() = updateInfo?.forced == true
    private var activeCard = ""
    private var authJob: Job? = null
    private var heartbeatJob: Job? = null
    private var generation = 0
    var announcement by mutableStateOf(preferences.getString("announcement_cache", "正在加载公告…") ?: "正在加载公告…"); private set
    var announcementCached by mutableStateOf(preferences.contains("announcement_cache")); private set
    var announcementNew by mutableStateOf(false); private set
    fun markAnnouncementRead() {
        preferences.edit().putString("announcement_read", announcementDigest(announcement)).apply()
        announcementNew = false
    }
    private fun announcementDigest(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it.toInt() and 255) }
    fun login() {
        if (authBusy || cardInput.isBlank()) return
        val card = cardInput.trim()
        if (updateRequired) { authStatus = "请先更新应用"; return }
        generation++; val request = generation
        authJob?.cancel(); heartbeatJob?.cancel(); authVerified = false; auth = null
        authBusy = true; authStatus = "正在验证…"
        authJob = viewModelScope.launch {
            try {
                val result = authorization.login(card)
                if (request != generation) return@launch
                if (result.durationType != "free" && result.expiresAt > 0 && result.expiresAt <= System.currentTimeMillis() / 1000) throw ApiRejected("152")
                authorization.remember(card, rememberCard)
                activeCard = card; auth = result; authVerified = true; authStatus = "已授权"
                beginHeartbeat(request)
                viewModelScope.launch { loadRemoteConfiguration(request) }
            } catch (cancelled: CancellationException) { throw cancelled }
              catch (failure: Exception) {
                if (request == generation) {
                    authStatus = when (failure) { is ApiRejected -> failure.message ?: "授权失效"; is InvalidResponse -> "响应校验失败"; else -> "网络或服务异常，请重试" }
                }
            } finally { if (request == generation) authBusy = false }
        }
    }
    fun logout() {
        generation++; authJob?.cancel(); heartbeatJob?.cancel(); auth = null; activeCard = ""; cardInput = ""
        authVerified = false; authBusy = false; secondsRemaining = 0; heartbeatStatus = "未登录"; authStatus = "未登录"
        authorization.clear(); rememberCard = false; remoteConfig = SkyRemoteConfig(); configStatus = "登录后加载远程配置"
    }
    private fun beginHeartbeat(request: Int) {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            var lastCheck = 0L
            while (isActive && generation == request) {
                val current = auth ?: break
                secondsRemaining = if (current.durationType != "free" && current.expiresAt > 0) (current.expiresAt - System.currentTimeMillis() / 1000).coerceAtLeast(0) else Long.MAX_VALUE
                if (current.durationType != "free" && current.expiresAt > 0 && secondsRemaining == 0L) {
                    authVerified = false; auth = null; activeCard = ""; authStatus = "授权已到期，请重新登录"; heartbeatStatus = "已到期"; break
                }
                val now = System.currentTimeMillis()
                if (now - lastCheck >= 30000) {
                    lastCheck = now
                    try { authorization.heartbeat(activeCard, current.token); heartbeatStatus = "验证正常"; authVerified = true; authStatus = "已授权" }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: MissingProtocol) { heartbeatStatus = "心跳接口未配置，本次仅登录验证" }
                    catch (_: ApiRejected) { authVerified = false; auth = null; activeCard = ""; authStatus = "授权失效，请重新登录"; heartbeatStatus = "服务端拒绝"; break }
                    catch (_: Exception) { authVerified = false; authStatus = "网络异常，授权待确认"; heartbeatStatus = "连接失败，30秒后重试" }
                }
                delay(1000)
            }
        }
    }
    private suspend fun loadRemoteConfiguration(request: Int) {
        configStatus = "正在加载远程配置…"
        try { val config = authorization.configuration(activeCard); if (generation == request) { remoteConfig = config; configStatus = "远程配置已更新" } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: MissingProtocol) { if (generation == request) configStatus = "远程变量接口未配置" }
        catch (_: Exception) { if (generation == request) configStatus = "远程配置加载失败" }
    }
    fun compatibleDrivers(): List<DriverEntry> = remoteConfig.compatibleDrivers(DriverDevice(Build.SUPPORTED_ABIS.toSet(), Build.VERSION.SDK_INT, System.getProperty("os.version") ?: ""), auth?.cardType)
    fun download(entry: DriverEntry) {
        if (updateRequired || !authVerified || remoteConfig.maintenance.enabled || !remoteConfig.featureEnabled("downloads")) { downloadError = "当前授权或远程配置不允许下载"; return }
        if (entry !in compatibleDrivers()) { downloadError = "驱动与当前设备或卡密类型不匹配"; return }
        viewModelScope.launch { try { downloadRepository.download(entry) } catch (_: Exception) { downloadError = "下载任务创建失败" } }
    }
    fun cancelDownload(id: String) { viewModelScope.launch { try { downloadRepository.cancel(id) } catch (_: Exception) { downloadError = "取消失败，请重试" } } }
    fun retryDownload(id: String) { compatibleDrivers().firstOrNull { it.id == id }?.let { download(it) } }
    fun deleteDownload(id: String) { viewModelScope.launch { try { downloadRepository.delete(id) } catch (_: Exception) { downloadError = "删除失败，请重试" } } }
    fun checkUpdate() {
        if (updateBusy) return
        updateBusy = true; updateStatus = "正在检查更新…"
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { WeiYanClient.update() }
                val local = getApplication<Application>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0).versionName ?: "0"
                updateInfo = if (newerVersion(result.version, local)) result else null
                updateStatus = if (updateInfo == null) "当前没有更高版本" else "发现新版本 ${result.version}"
            } catch (cancelled: CancellationException) { throw cancelled }
              catch (_: Exception) { updateStatus = "更新检查失败，请重试" }
            finally { updateBusy = false }
        }
    }
    /* ---------- 终端 ---------- */
    var termLog by mutableStateOf("> sky 终端（root）\n> 输入命令回车执行，支持 cd，也可直接运行 .sh 脚本\n")
        private set
    var termInput by mutableStateOf("")
        private set
    var termRunning by mutableStateOf(false)
        private set
    private var termCwd = "/data/local/tmp"

    fun updateTermInput(value: String) { termInput = value }

    fun clearTerminal() { termLog = "> 已清空\n" }

    private fun appendTerm(line: String) {
        val next = termLog + line + "\n"
        termLog = if (next.length > 60000) next.substring(next.length - 60000) else next
    }

    private fun execRoot(command: String, onLine: (String) -> Unit): Int = try {
        val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
        process.inputStream.bufferedReader().forEachLine { if (it.isNotBlank()) onLine(it) }
        process.waitFor()
        process.exitValue()
    } catch (error: Throwable) {
        onLine("执行失败：${error.message}")
        -1
    }

    fun runTerminalCommand() {
        val command = termInput.trim()
        if (command.isEmpty() || termRunning) return
        termInput = ""
        if (command == "clear") { clearTerminal(); return }
        appendTerm("\$ $command")
        termRunning = true
        viewModelScope.launch(Dispatchers.IO) {
            if (command.startsWith("cd ")) {
                val target = command.substring(3).trim().trim('"', '\'')
                val resolved = StringBuilder()
                execRoot("cd $termCwd 2>/dev/null; cd \"$target\" 2>&1 && pwd") { resolved.append(it) }
                val dir = resolved.toString().lineSequence().lastOrNull { it.startsWith("/") }
                if (dir != null) {
                    termCwd = dir.trim()
                    appendTerm("> 当前目录：$termCwd")
                } else appendTerm("> 目录不存在：$target")
            } else {
                val code = execRoot("cd $termCwd 2>/dev/null; $command") { appendTerm(it) }
                if (code != 0) appendTerm("[退出码 $code]")
            }
            termRunning = false
        }
    }

    fun runTerminalScript(uri: Uri) {
        termRunning = true
        appendTerm("> 读取脚本…")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val script = File(app.filesDir, "term_script.sh")
                app.contentResolver.openInputStream(uri)?.use { input ->
                    script.outputStream().use { output -> input.copyTo(output) }
                } ?: throw IllegalStateException("无法读取所选文件")
                appendTerm("> 已保存到 ${script.absolutePath}")
                val code = execRoot("cp '${script.absolutePath}' /data/local/tmp/term_script.sh && chmod 777 /data/local/tmp/term_script.sh && cd $termCwd && sh /data/local/tmp/term_script.sh 1") { appendTerm(it) }
                appendTerm("[退出码 $code]")
            } catch (error: Throwable) {
                appendTerm("> 执行失败：${error.message}")
            }
            termRunning = false
        }
    }

    override fun onCleared() { authJob?.cancel(); heartbeatJob?.cancel(); viewModelScope.launch(Dispatchers.IO + kotlinx.coroutines.NonCancellable) { downloadRepository.close() }; super.onCleared() }

    var announcementLoading by mutableStateOf(false); private set
    init {
        refreshAnnouncement(); checkUpdate()
        try { authorization.rememberedCard()?.let { cardInput = it; rememberCard = true; login() } } catch (_: Exception) { authStatus = "已保存的卡密不可读，请重新输入" }
    }
    fun refreshAnnouncement() {
        if (announcementLoading) return
        announcementLoading = true
        viewModelScope.launch {
            try {
                announcement = withContext(Dispatchers.IO) { WeiYanClient.notice() }
                announcementCached = false
                preferences.edit().putString("announcement_cache", announcement).apply()
                announcementNew = preferences.getString("announcement_read", "") != announcementDigest(announcement)
            }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { announcementCached = preferences.contains("announcement_cache"); if (!announcementCached) announcement = "公告暂时无法加载。" }
            finally { announcementLoading = false }
        }
    }
    fun updateDebug(value: DebugOptions) {
        debug = value
        preferences.edit().apply {
            putBoolean("debug_squircle", value.squircle)
            putBoolean("debug_haptic", value.haptic); putBoolean("debug_user_scroll", value.userScroll)
            putBoolean("debug_top_bar", value.topBar); putBoolean("debug_small_top_bar", value.smallTopBar)
            putBoolean("debug_navigation", value.navigation); putBoolean("debug_badge", value.badge)
            putBoolean("debug_toolbar", value.toolbar); putBoolean("debug_fab", value.fab)
            putBoolean("debug_corner_clip", value.cornerClip); putBoolean("debug_dim", value.dim)
            putBoolean("debug_block_input", value.blockInput)
            putInt("debug_navigation_mode", value.navigationMode); putInt("debug_transition", value.transition)
        }.apply()
    }
    fun resetDebug() {
        updateDebug(DebugOptions()); selectPageGesture("cross"); setOption("swipe_back", true)
        setOption("blur", true); setOption("floating_bar", true)
    }
    fun setOption(key: String, value: Boolean) {
        when (key) {
            "blur" -> blur = value
            "floating_bar" -> floatingBar = value
            "liquid_glass" -> liquidGlass = value
            "predictive_back" -> predictiveBack = value
            "swipe_back" -> swipeBack = value
        }
        preferences.edit().putBoolean(key, value).apply()
    }
    fun selectScale(value: Int) { scale = value.coerceIn(80, 120); preferences.edit().putInt("ui_scale", scale).apply() }
    fun selectPageGesture(value: String) { pageGesture = value; preferences.edit().putString("page_gesture", value).apply() }
    var page by mutableIntStateOf(0)
    var driverName by mutableStateOf("未选择驱动文件")
    fun selectTheme(value: String) { themeMode = value; preferences.edit().putString("theme_mode", value).apply() }
}
