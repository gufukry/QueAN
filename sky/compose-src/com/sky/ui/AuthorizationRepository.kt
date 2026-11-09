package com.sky.ui

import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class Authorization(
    val expiresAt: Long,
    val cardType: String,
    val durationType: String,
    val note: String,
    val token: String,
)

internal class ApiRejected(val status: String) : Exception("服务拒绝请求，状态码 $status")
internal class InvalidResponse : Exception("服务响应校验失败")
internal class MissingProtocol : Exception("接口协议尚未配置")

internal class AuthorizationRepository(context: Context) {
    private val app = context.applicationContext
    private val store = SecureCardStore(app)
    fun rememberedCard(): String? = store.read()
    fun remember(card: String, enabled: Boolean) { if (enabled) store.save(card) else store.clear() }
    fun clear() { store.clear() }
    suspend fun login(card: String): Authorization = withContext(Dispatchers.IO) {
        val device = Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID)
            ?: throw IllegalStateException("无法读取设备标识")
        WeiYanClient.login(card, device)
    }
    suspend fun heartbeat(card: String, token: String): Unit = withContext(Dispatchers.IO) {
        val device = Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID)
            ?: throw IllegalStateException("无法读取设备标识")
        WeiYanClient.heartbeat(card, device, token)
    }
    suspend fun configuration(card: String): SkyRemoteConfig = withContext(Dispatchers.IO) {
        val device = Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID)
            ?: throw IllegalStateException("无法读取设备标识")
        parseRemoteConfig(WeiYanClient.configuration(card, device))
    }
}
