package com.minivision.moe.util

import com.minivision.moe.core.LOG_D
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.xor

object MoeUtils {

    /**
     * Byte[]转成十六进制
     */
    fun byteToHexString(data: ByteArray?): String {
        val sb = StringBuilder()
        if (data != null) {
            for (i in data.indices) {
                var tempHexStr = Integer.toHexString(data[i].toInt() and (0xFF))
                        .toUpperCase(Locale.getDefault()) + " "
                tempHexStr = if (tempHexStr.length == 2) "0$tempHexStr" else tempHexStr
                sb.append(tempHexStr)
            }
        }
        return sb.toString()
    }

    /**
     * Byte[]转成十六进制
     * @param length 转换后的长度
     */
    fun byteToHexString(data: ByteArray?, length: Int): String {
        val sb = StringBuilder()
        if (data != null) {
            for (i in data.indices) {
                var tempHexStr = Integer.toHexString(data[i].toInt() and (0xFF))
                        .toUpperCase(Locale.getDefault()) + " "
                tempHexStr = if (tempHexStr.length == 2) "0$tempHexStr" else tempHexStr
                sb.append(tempHexStr)
            }
            if (length > data.size) {
                for (i in 0 until length - data.size) {
                    sb.append("00 ")
                }
            }
            if (length < data.size) {
                sb.delete(length * 3, data.size * 3)
            }
        }
        return sb.toString()
    }

    /**
     * 16进制表示的字符串转换为字节数组
     */
    fun hexToByteArray(hexString: String): ByteArray {
        val temp = hexString.replace(" ".toRegex(), "")
        val len = temp.length
        val bytes = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            bytes[i / 2] = ((Character.digit(temp[i], 16) shl 4) + Character
                    .digit(temp[i + 1], 16)).toByte()
            i += 2
        }
        return bytes
    }

    /**
     * 16进制表示的字符串转换为字节数组
     */
    fun hexToByteArrayFilter0(hexString: String): ByteArray {
        val temp = hexString.replace(" ".toRegex(), "").replace("00".toRegex(), "")
        val len = temp.length
        val bytes = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            bytes[i / 2] = ((Character.digit(temp[i], 16) shl 4) + Character
                    .digit(temp[i + 1], 16)).toByte()
            i += 2
        }
        return bytes
    }

    /**
     * Byte[]转换成Int
     */
    fun byteArrayToInt(b: ByteArray): Int {
        var res = 0
        for (i in b.indices) {
            res += b[i].toInt() and 0xff shl i * 8
        }
        return res
    }

    /**
     * 十六进制字符串转成Int
     */
    fun hexToInt(hexString: String): Int {
        return byteArrayToInt(hexToByteArray(hexString))
    }

    /**
     * Int数值转换成十六进制字符串
     * 按照小端排序
     */
    fun intToLHex(n: Int, length: Int): String {
        val b = ByteArray(4)
        b[0] = (n and 0xff).toByte()
        b[1] = (n shr 8 and 0xff).toByte()
        b[2] = (n shr 16 and 0xff).toByte()
        b[3] = (n shr 24 and 0xff).toByte()
        return byteToHexString(b, length)
    }

    /**
     * Int转成2位十六进制
     */
    fun shortToHex(n: Int): String {
        val b = ByteArray(2)
        b[0] = (n and 0xff).toByte()
        b[1] = (n shr 8 and 0xff).toByte()
        return byteToHexString(b)
    }

    /**
     * 字符串转换成16进制字符串
     */
    fun string2Hex(s: String): String {
        return byteToHexString(s.toByteArray())
    }

    /**
     * 字符串转换成16进制字符串
     */
    fun string2Hex(s: String, length: Int): String {
        return byteToHexString(s.toByteArray(), length)
    }

    /**
     * 16进制字符串转换成字符串
     */
    fun hexToString(hexString: String): String {
        return String(hexToByteArrayFilter0(hexString))
    }

    fun getXor(datas: ByteArray): String {
        var temp = datas[0]
        for (i in 1 until datas.size) {
            temp = temp xor datas[i]
        }
        var result = Integer.toHexString(temp.toInt() and 0xFF)
        if (result.length == 1) {
            result = "0$result"
        }
        return result
    }

    fun getXor(data: String): String {
        return getXor(hexToByteArray(string2Hex(data)))
    }

    /**
     *
     */
    fun getXorFromHex(data: String): String {
        return getXor(hexToByteArray(data))
    }

    /**
     * 生成16位不重复的随机数，含数字+大小写
     * @return
     */
    fun getGUID(): String {
        val uid = StringBuilder()
        //产生16位的强随机数
        val rd: Random = SecureRandom()
        for (i in 0..15) {
            //产生0-2的3位随机数
            when (rd.nextInt(3)) {
                0 ->           //0-9的随机数
                    uid.append(rd.nextInt(10))
                1 ->           //ASCII在65-90之间为大写,获取大写随机
                    uid.append((rd.nextInt(25) + 65).toChar())
                2 ->           //ASCII在97-122之间为小写，获取小写随机
                    uid.append((rd.nextInt(25) + 97).toChar())
                else -> {
                }
            }
        }
        return uid.toString()
    }

    fun parseMessage(hex: String) {
        val bodyArray = hexToByteArray(hex)
        val lengthBuf = ByteBuffer.allocate(4)
        lengthBuf.put(bodyArray, 0, 4)
        val bodyLength = MoeUtils.byteArrayToInt(lengthBuf.array())
        LOG_D("bodyLength:$bodyLength")

        val cmdBuf = ByteBuffer.allocate(2)
        cmdBuf.put(bodyArray, 13, 2)
        val cmd = MoeUtils.byteArrayToInt(cmdBuf.array())

        val reqIdBuf = ByteBuffer.allocate(16)
        reqIdBuf.put(bodyArray, 15, 16)
        val reqId = String(reqIdBuf.array())

        val bodyBuf = ByteBuffer.allocate(bodyLength)
        bodyBuf.put(bodyArray, 31, bodyLength)
        var body = MoeUtils.byteToHexString(bodyBuf.array())

        val statusBuf = ByteBuffer.allocate(1)
        statusBuf.put(bodyArray, 31 + bodyLength, 1)
        val statusHex = MoeUtils.byteToHexString(statusBuf.array())
        val status = MoeUtils.hexToInt(statusHex)
    }


    /**
     * 获取现在BCD格式日期
     */
    fun getBcdTime(): String {
        val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return format.format(Date()).chunked(2).joinToString(" ") + " "
    }

    /**
     * 通过时间戳转换成BCD格式日期
     */
    fun getBcdTime(timestamp: Long): String {
        val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return format.format(Date(timestamp)).chunked(2).joinToString(" ") + " "
    }

    @JvmStatic
    fun main(args: Array<String>) {
//        print(intToLHex(257,2) )
//        println(1 and 0XFF shl 8 xor 1)
//        val a="test".also {
//            println(it.length)
//            200
//        }
//        println(a)
        print(getBcdTime())
    }
}