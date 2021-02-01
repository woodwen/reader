package com.woodnoisu.reader.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

/**
 * MD5帮助类
 */
object MD5Util {
    /**
     * 字符串转md5
     */
    fun strToMd5By16(str: String): String {
        var reStr = strToMd5By32(str)
        if (reStr != null) {
            reStr = reStr.substring(8, 24)
        }
        return reStr
    }

    /**
     * 字符串转md5
     */
    private fun strToMd5By32(str: String): String {
        var reStr = ""
        try {
            val md5 = MessageDigest.getInstance("MD5")
            val bytes = md5.digest(str.toByteArray())
            val stringBuffer = StringBuilder()
            for (b in bytes) {
                val bt = b and 0xff.toByte()
                if (bt < 16) {
                    stringBuffer.append(0)
                }
                stringBuffer.append(Integer.toHexString(bt.toInt()))
            }
            reStr = stringBuffer.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return reStr
    }
}
