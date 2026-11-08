package com.quan.quan

import java.security.MessageDigest

/** 卡密登录结果 */
data class LoginResult(val ok: Boolean, val msg: String, val expireText: String, val kami: String = "")

private fun rc4hex(data: String, key: String): String {
    val out = rc4(data.toByteArray(), key.toByteArray())
    val sb = StringBuilder()
    for (b in out) sb.append((b.toInt() and 0xFF).toString(16).padStart(2, '0'))
    return sb.toString()
}

fun md5(s: String): String {
    val d = MessageDigest.getInstance("MD5").digest(s.toByteArray())
    val sb = StringBuilder()
    for (b in d) sb.append((b.toInt() and 0xFF).toString(16).padStart(2, '0'))
    return sb.toString()
}

private fun jsonInt(js: String, k: String): Int =
    Regex("\"" + k + "\"\\s*:\\s*(-?\\d+)").find(js)?.groupValues?.get(1)?.toIntOrNull() ?: 0

private fun jsonStr(js: String, k: String): String =
    Regex("\"" + k + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(js)
        ?.groupValues?.get(1)?.replace("\\\"", "\"") ?: ""

/** 单次登录：返回 (成功?, 提示, 到期文案) */
private fun loginOnce(kami: String, deviceId: String): LoginResult {
    val t = System.currentTimeMillis() / 1000L
    val sign = md5("kami=$kami&markcode=$deviceId&t=$t&${Wy.APPKEY}")
    val value = t.toString() + (100000 + (Math.random() * 900000).toInt())
    val plain = "kami=$kami&markcode=$deviceId&t=$t&sign=$sign&value=$value"
    val enc = rc4hex(plain, Wy.RC4KEY)
    val url = "https://${Wy.HOST}/api/?id=kmlogin&app=${Wy.APPID}"
    val resp = httpPost(url, "&data=$enc") ?: httpGet("$url&data=$enc")
    if (resp.isNullOrEmpty()) return LoginResult(false, "服务器没有响应（检查网络）", "")

    var js = rc4unhex(resp, Wy.RC4KEY)
    if (js == null || !js.contains('{')) js = resp
    if (!js.contains('{')) return LoginResult(false, "返回内容无法解析", "")

    var code = jsonInt(js, "code")
    val msg = jsonStr(js, "msg")
    var remain = 0L
    var expire = 0L
    if (code == 0) {
        // 字段名是哈希串，按数值范围推断：三位数=返回码，时间戳=到期，秒数=剩余
        Regex("\"([0-9a-z]{28,40})\"\\s*:\\s*(-?\\d+)").findAll(js).forEach { m ->
            val v = m.groupValues[2].toLongOrNull() ?: return@forEach
            if (v in 100..999 && code == 0) code = v.toInt()
            else if (v in 1600000000L..4102444800L) expire = maxOf(expire, v)
            else if (v in 1001L..100000000L) remain = maxOf(remain, v)
        }
    }
    if (code != 355 && code != 967) {
        val m = if (msg.isBlank()) "卡密无效或已过期" else msg
        return LoginResult(false, "$m（返回码 $code）", "")
    }
    val left = if (remain > 0) "剩余 ${remain / 86400} 天" else ""
    val end = if (expire > 0) {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
            .format(java.util.Date(expire * 1000L)) + " 到期"
    } else ""
    val text = listOf(left, end).filter { it.isNotEmpty() }.joinToString("，")
    return LoginResult(true, "验证通过", text, kami)
}

/**
 * 微验卡密登录：和 Java 版一致——带 markcode / t / sign / value，
 * 卡密没写 @ 前缀时自动再试一次 "@卡密"。
 */
fun wyLogin(kamiRaw: String, deviceId: String): LoginResult {
    val kami = kamiRaw.trim()
    if (kami.isEmpty()) return LoginResult(false, "请输入卡密", "")
    val cands = if (kami.startsWith("@")) listOf(kami) else listOf(kami, "@$kami")
    var err = "卡密不存在"
    for (c in cands) {
        val r = loginOnce(c, deviceId)
        if (r.ok) return r
        err = r.msg
        if (!err.contains("不存在")) return r
    }
    return LoginResult(false, err, "")
}
