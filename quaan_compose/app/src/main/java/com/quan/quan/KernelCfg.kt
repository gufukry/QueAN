package com.quan.quan

import android.util.Base64
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

/** 内核配置码（HXCFG1.）导入/导出，与 Java 版一致 */
object KernelCfg {
    const val CFG_API = "https://xffaka.cc.cd/hp/cfg.php"
    private const val INNER_MAGIC = 0x48584331   // "HXC1"
    private const val FILE_MAGIC = 0x48584345    // "HXCE"
    const val PREFIX = "HXCFG1."

    /** 解析配置码，成功返回 null，失败返回原因；成功时把 cfg 字节交给回调写盘 */
    fun decode(code: String): Pair<ByteArray?, String?> {
        val raw = code.trim()
        if (!raw.startsWith(PREFIX)) return null to "不是配置码（应以 HXCFG1. 开头）"
        return try {
            val bin = Base64.decode(raw.substring(PREFIX.length), Base64.DEFAULT)
            if (bin.size < 12) return null to "配置码内容损坏"
            val bb = ByteBuffer.wrap(bin).order(ByteOrder.LITTLE_ENDIAN)
            val magic = bb.int
            val sz = bb.int
            if (magic != INNER_MAGIC) return null to "配置码版本不匹配"
            if (sz <= 0 || sz > 64 * 1024 || bin.size != 8 + sz + 4) return null to "配置码长度异常"
            val crc = CRC32()
            crc.update(bin, 0, 8 + sz)
            val want = ByteBuffer.wrap(bin, 8 + sz, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (crc.value.toInt() != want) return null to "配置码校验失败"

            val cfg = ByteArray(sz)
            System.arraycopy(bin, 8, cfg, 0, sz)
            val file = ByteArray(8 + sz)
            val fb = ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN)
            fb.putInt(FILE_MAGIC)
            fb.putInt(sz)
            System.arraycopy(cfg, 0, file, 8, sz)
            file to null
        } catch (t: Throwable) {
            null to "配置码解析失败：${t.message}"
        }
    }

    /** 从服务器按数字码取配置码 */
    fun fetchByNumber(num: String): String? {
        val out = httpGet("$CFG_API?act=get&id=$num") ?: return null
        val t = out.trim()
        return if (t.startsWith(PREFIX)) t else null
    }

    /** 把 cfg 字节落盘到内核读取的位置 */
    fun write(cfgFile: ByteArray, filesDir: File, sh: (String) -> String): Boolean {
        return try {
            val local = File(filesDir, "imp_cfg.bin")
            local.writeBytes(cfgFile)
            sh(
                "mkdir -p /data/local/tmp; cp '${local.absolutePath}' /data/local/tmp/hx_cfg.bin; " +
                        "chmod 666 /data/local/tmp/hx_cfg.bin; " +
                        "cp '${local.absolutePath}' /sdcard/hx_cfg.bin 2>/dev/null"
            )
            true
        } catch (t: Throwable) {
            false
        }
    }
}
