package com.quan.quan

import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** 微验（卡密/公告）服务参数，与 Java 版保持一致 */
object Wy {
    const val HOST = "wy.llua.cn"
    const val APPID = "34202"
    const val RC4KEY = "uc79fbccf0cd3cb613793ef317a3eeb7d8b"
    const val APPKEY = "j678e201b22de2975315e2b186183fd8"
    const val UA = "Mozilla/5.0 (Linux; Android) QueAN"
}

fun httpGet(url: String): String? = try {
    val c = URL(url).openConnection() as HttpURLConnection
    c.connectTimeout = 9000
    c.readTimeout = 9000
    c.instanceFollowRedirects = true
    c.setRequestProperty("User-Agent", Wy.UA)
    c.setRequestProperty("Accept", "*/*")
    val code = c.responseCode
    if (code >= 400) { c.disconnect(); null }
    else c.inputStream.bufferedReader().use { it.readText() }.also { c.disconnect() }
} catch (t: Throwable) {
    null
}

fun httpPost(url: String, body: String): String? = try {
    val c = URL(url).openConnection() as HttpURLConnection
    c.requestMethod = "POST"
    c.connectTimeout = 9000
    c.readTimeout = 9000
    c.doOutput = true
    c.instanceFollowRedirects = true
    c.setRequestProperty("User-Agent", Wy.UA)
    c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    c.outputStream.use { it.write(body.toByteArray()) }
    val code = c.responseCode
    if (code >= 400) { c.disconnect(); null }
    else c.inputStream.bufferedReader().use { it.readText() }.also { c.disconnect() }
} catch (t: Throwable) {
    null
}

/** RC4：hex 字符串解成明文 */
fun rc4unhex(hex: String, key: String): String? = try {
    val h = hex.trim()
    val data = ByteArray(h.length / 2) { i ->
        h.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
    String(rc4(data, key.toByteArray()), Charsets.UTF_8)
} catch (t: Throwable) {
    null
}

fun rc4(data: ByteArray, key: ByteArray): ByteArray {
    val s = IntArray(256) { it }
    var j = 0
    for (i in 0 until 256) {
        j = (j + s[i] + (key[i % key.size].toInt() and 0xFF)) and 0xFF
        val t = s[i]; s[i] = s[j]; s[j] = t
    }
    val out = ByteArray(data.size)
    var i = 0
    j = 0
    for (k in data.indices) {
        i = (i + 1) and 0xFF
        j = (j + s[i]) and 0xFF
        val t = s[i]; s[i] = s[j]; s[j] = t
        out[k] = (data[k].toInt() xor s[(s[i] + s[j]) and 0xFF]).toByte()
    }
    return out
}

/** 拉公告（先 GET 再 POST），返回原文 */
fun fetchNotice(): String? {
    val base = "/api/?id=notice&app=${Wy.APPID}&_t=${System.currentTimeMillis()}"
    val urls = listOf("https://${Wy.HOST}$base", "http://${Wy.HOST}$base")
    var resp: String? = null
    for (pass in 0..1) {
        if (!resp.isNullOrEmpty()) break
        for (u in urls) {
            resp = if (pass == 0) httpGet(u) else httpPost(u, "&app=${Wy.APPID}")
            if (!resp.isNullOrEmpty()) break
        }
    }
    if (resp.isNullOrEmpty()) return null
    var js = rc4unhex(resp, Wy.RC4KEY)
    if (js == null || !js.contains('{')) js = resp
    val m = Regex("\"app_gg\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(js) ?: return null
    return m.groupValues[1]
        .replace("\\r\\n", "\n")
        .replace("\\n", "\n")
        .replace("\\r", "")
        .replace("\\\"", "\"")
        .trim()
}

/** 下载地址是否像个真链接（域名字符集限定 ASCII，占位符直接判无效） */
fun isDownloadUrl(u: String): Boolean =
    Regex("^https?://[A-Za-z0-9.-]+(:[0-9]+)?/").containsMatchIn(u)

/** 取某个游戏的 [更新...] 段落（不含其它游戏那段） */
fun updateBlock(text: String, tag: String): String? {
    val key = "[更新$tag]"
    val i = text.lowercase().indexOf(key.lowercase())
    if (i < 0) return null
    var rest = text.substring(i + key.length)
    val nxt = rest.lowercase().indexOf("[更新")
    if (nxt >= 0) rest = rest.substring(0, nxt)
    return rest
}

/** 把公告里所有 [更新...] 前缀去掉，只留说明文字 */
fun stripUpdateLines(text: String): String =
    text.replace(Regex("(?i)\\[更新[^\\]]*][^|\\n]*(\\|[^|\\n]*){0,2}"), "")
        .replace("\\n", "\n")
        .trim()

/** 下载到 filesDir，返回文件；onProgress(已下载, 总大小) */
fun downloadTo(url: String, dst: File, onProgress: (Long, Long) -> Unit): Boolean {
    var c: HttpURLConnection? = null
    return try {
        c = URL(url).openConnection() as HttpURLConnection
        c.instanceFollowRedirects = true
        c.connectTimeout = 10000
        c.readTimeout = 20000
        c.setRequestProperty("User-Agent", Wy.UA)
        c.setRequestProperty("Accept", "*/*")
        if (c.responseCode >= 400) return false
        val total = c.contentLengthLong
        val tmp = File(dst.absolutePath + ".part")
        c.inputStream.use { input ->
            tmp.outputStream().use { out ->
                val buf = ByteArray(16384)
                var got = 0L
                while (true) {
                    val n = input.read(buf)
                    if (n <= 0) break
                    out.write(buf, 0, n)
                    got += n
                    onProgress(got, total)
                }
            }
        }
        if (tmp.length() < 1024) return false
        if (dst.exists()) dst.delete()
        tmp.renameTo(dst)
    } catch (t: Throwable) {
        false
    } finally {
        c?.disconnect()
    }
}

/** 版本号比较：a>b 返回 1，相等 0，小于 -1 */
fun compareVersion(a: String, b: String): Int {
    val x = a.trim().split(".", "-", "_")
    val y = b.trim().split(".", "-", "_")
    for (i in 0 until maxOf(x.size, y.size)) {
        val xi = x.getOrNull(i)?.toIntOrNull() ?: 0
        val yi = y.getOrNull(i)?.toIntOrNull() ?: 0
        if (xi != yi) return if (xi > yi) 1 else -1
    }
    return 0
}
