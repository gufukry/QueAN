package com.sky.ui

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal object WeiYanClient {
    private fun request(action: String, data: String? = null): JSONObject {
        if (BuildConfig.WEIYAN_RC4_KEY.isBlank()) throw MissingProtocol()
        require(action.matches(Regex("[a-zA-Z0-9_]+")))
        val endpoint = "https://wy.llua.cn/api/?id=$action&app=${BuildConfig.WEIYAN_APP_ID}" +
            (data?.let { "&data=" + Rc4Codec.encryptHex(it, BuildConfig.WEIYAN_RC4_KEY) } ?: "")
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 12000; connection.readTimeout = 12000
            connection.instanceFollowRedirects = false
            if (connection.responseCode != 200) throw IOException("云端 HTTP ${connection.responseCode}")
            val bytes = connection.inputStream.use { it.readNBytes(262145) }
            if (bytes.size > 262144) throw IOException("响应超过大小限制")
            return JSONObject(Rc4Codec.decryptHex(bytes.toString(Charsets.UTF_8).trim(), BuildConfig.WEIYAN_RC4_KEY))
        } finally { connection.disconnect() }
    }
    private fun payload(response: JSONObject): JSONObject {
        val msg = response.get("msg")
        return if (msg is JSONObject) msg else JSONObject(msg.toString())
    }
    fun notice(): String {
        val result = request("notice")
        if (result.optInt("code") != 200) throw ApiRejected(result.optString("code"))
        return payload(result).getString("app_gg").ifBlank { "暂无公告" }
    }
    fun update(): AppUpdate {
        val result = request("ini")
        if (result.optInt("code") != 200) throw ApiRejected(result.optString("code"))
        val data = payload(result)
        val url = data.optString("app_update_url")
        if (url.isNotEmpty() && !isHttpsUrl(url)) throw InvalidResponse()
        return AppUpdate(data.getString("version"), data.optString("app_update_show"), url, data.optString("app_update_must") == "y")
    }
    private fun md5(value: String) = java.security.MessageDigest.getInstance("MD5").digest(value.toByteArray()).joinToString("") { "%02x".format(it.toInt() and 255) }
    private fun authenticated(action: String, card: String, device: String, expectedCode: String, extra: List<Pair<String, String>> = emptyList()): JSONObject {
        if (BuildConfig.WEIYAN_APP_KEY.isBlank()) throw MissingProtocol()
        if (card.any { it == '&' || it == '=' || it.isISOControl() }) throw IllegalArgumentException("卡密包含不支持的字符")
        val time = System.currentTimeMillis().toString()
        val value = java.security.SecureRandom().nextLong().toULong().toString()
        val fields = listOf("kami" to card, "markcode" to device, "t" to time) + extra
        val canonical = fields.joinToString("&") { "${it.first}=${it.second}" }
        val sign = md5(canonical + "&" + BuildConfig.WEIYAN_APP_KEY)
        val response = request(action, "$canonical&sign=$sign&value=$value")
        if (response.optString("code") != expectedCode) throw ApiRejected(response.optString("code"))
        val serverTime = response.getString("time")
        val seconds = serverTime.toLongOrNull() ?: throw InvalidResponse()
        val normalized = if (seconds > 100000000000L) seconds / 1000 else seconds
        if (kotlin.math.abs(normalized - System.currentTimeMillis() / 1000) > 120) throw InvalidResponse()
        val expected = md5(serverTime + BuildConfig.WEIYAN_APP_KEY + value)
        if (!java.security.MessageDigest.isEqual(expected.toByteArray(), response.optString("check").toByteArray())) throw InvalidResponse()
        return payload(response)
    }
    fun login(card: String, device: String): Authorization {
        val data = authenticated("kmlogon", card, device, BuildConfig.WEIYAN_LOGIN_CODE)
        val expiry = data.getString("vip").toLongOrNull() ?: throw InvalidResponse()
        return Authorization(expiry, data.optString("ktype"), data.optString("kmtype"), data.optString("note"), data.optString("token"))
    }
    fun heartbeat(card: String, device: String, token: String) {
        if (BuildConfig.WEIYAN_HEARTBEAT_ACTION.isBlank() || BuildConfig.WEIYAN_HEARTBEAT_CODE.isBlank() || token.isBlank()) throw MissingProtocol()
        authenticated(BuildConfig.WEIYAN_HEARTBEAT_ACTION, card, device, BuildConfig.WEIYAN_HEARTBEAT_CODE, listOf("kamitoken" to token))
    }
    fun configuration(card: String, device: String): String {
        if (BuildConfig.WEIYAN_VARIABLE_ACTION.isBlank() || BuildConfig.WEIYAN_VARIABLE_FIELD.isBlank() || BuildConfig.WEIYAN_VARIABLE_NAME.isBlank() || BuildConfig.WEIYAN_VARIABLE_CODE.isBlank()) throw MissingProtocol()
        val result = authenticated(BuildConfig.WEIYAN_VARIABLE_ACTION, card, device, BuildConfig.WEIYAN_VARIABLE_CODE,
            listOf(BuildConfig.WEIYAN_VARIABLE_FIELD to BuildConfig.WEIYAN_VARIABLE_NAME))
        return result.getString("msg")
    }

}

internal data class AppUpdate(val version: String, val notes: String, val url: String, val forced: Boolean)
internal fun isHttpsUrl(value: String): Boolean = try { java.net.URI(value).let { it.scheme == "https" && !it.host.isNullOrBlank() && it.userInfo == null } } catch (_: Exception) { false }
internal fun newerVersion(remote: String, local: String): Boolean {
    fun parts(value: String): List<Int>? = value.removePrefix("v").split('.').map { it.toIntOrNull() ?: return null }
    val a = parts(remote) ?: return false; val b = parts(local) ?: return false
    for (index in 0 until maxOf(a.size, b.size)) {
        val diff = a.getOrElse(index) { 0 }.compareTo(b.getOrElse(index) { 0 })
        if (diff != 0) return diff > 0
    }
    return false
}

internal object Rc4Codec {
    fun encryptHex(value: String, key: String): String = transform(value.toByteArray(Charsets.UTF_8), key.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it.toInt() and 255) }
    fun transform(input: ByteArray, key: ByteArray): ByteArray {
        require(key.isNotEmpty())
        val box = IntArray(256) { it }
        var j = 0
        for (i in box.indices) {
            j = (j + box[i] + (key[i % key.size].toInt() and 255)) and 255
            val t = box[i]; box[i] = box[j]; box[j] = t
        }
        var x = 0; j = 0
        return ByteArray(input.size) { index ->
            x = (x + 1) and 255; j = (j + box[x]) and 255
            val t = box[x]; box[x] = box[j]; box[j] = t
            (input[index].toInt() xor box[(box[x] + box[j]) and 255]).toByte()
        }
    }
    fun decryptHex(encoded: String, key: String): String {
        require(encoded.isNotEmpty() && encoded.all { it.digitToIntOrNull(16) != null }) { "公告响应不是案例约定的十六进制数据" }
        val hex = if (encoded.length % 2 == 0) encoded else "0$encoded"
        val input = ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
        return transform(input, key.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8)
    }
}
