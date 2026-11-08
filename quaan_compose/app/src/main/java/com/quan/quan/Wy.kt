package com.quan.quan

/** 卡密登录结果 */
data class LoginResult(val ok: Boolean, val msg: String, val expireText: String)

private fun rc4hex(data: String, key: String): String {
    val out = rc4(data.toByteArray(), key.toByteArray())
    val sb = StringBuilder()
    for (b in out) sb.append(String.format("%02x", b.toInt() and 0xFF))
    return sb.toString()
}

private fun jsonInt(js: String, k: String): Int =
    Regex("\"$k\"\\s*:\\s*(-?\\d+)").find(js)?.groupValues?.get(1)?.toIntOrNull() ?: 0

private fun jsonStr(js: String, k: String): String =
    Regex("\"$k\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(js)
        ?.groupValues?.get(1)?.replace("\\\"", "\"") ?: ""

/** 微验卡密登录：与 Java 版同一套参数与解析 */
fun wyLogin(kami: String): LoginResult {
    val plain = "kami=$kami&app=${Wy.APPID}"
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
        // 字段名是哈希串，按数值范围猜：三位数=返回码，时间戳=到期，秒数=剩余
        Regex("\"([0-9a-z]{28,40})\"\\s*:\\s*(-?\\d+)").findAll(js).forEach { m ->
            val v = m.groupValues[2].toLongOrNull() ?: return@forEach
            if (v in 100..999 && code == 0) code = v.toInt()
            else if (v in 1600000000L..4102444800L) expire = maxOf(expire, v)
            else if (v in 1001L..100000000L) remain = maxOf(remain, v)
        }
    }
    if (code != 355 && code != 967) {
        return LoginResult(false, (msg.ifBlank { "卡密无效或已过期" }) + "（返回码 $code）", "")
    }
    val left = if (remain > 0) "剩余 ${remain / 86400} 天" else ""
    val end = if (expire > 0) {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
            .format(java.util.Date(expire * 1000L)) + " 到期"
    } else ""
    val text = listOf(left, end).filter { it.isNotEmpty() }.joinToString("，")
    return LoginResult(true, "验证通过", text)
}
