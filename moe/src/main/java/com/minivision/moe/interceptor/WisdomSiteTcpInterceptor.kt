package com.minivision.moe.interceptor

import com.minivision.moe.meta.LEFT_BRACKET
import com.minivision.moe.meta.SPLIT_HEAD
import org.json.JSONObject

/**
 *广联达项目智慧工地使用的拦截器
 * 消息格式: (CMD 1.0\r\n\r\nBODY\r\n)  CMD格式:DevStatus  BODY格式:a='123' b='123'
 * 此消息不利于JSON解析，故转换成JSON解析格式，{"cmd":CMD,"data":{}}
 * 例：
 *      原消息：(DevStatus 1.0\r\n\r\nsn="10001" ip="192.168.0.251"\r\n)
 *      头部：'('
 *      尾部：'\r\n)'
 *      拦截后消息:{"cmd":"DevStatus","data":{"ip":"192.168.0.251","sn":"10001"}}
 * @author gf
 * @date 2021/4/23
 */
class WisdomSiteTcpInterceptor : CommonTcpInterceptor() {
    override fun getHeadInterceptChar(): CharSequence {
        return LEFT_BRACKET.toString()
    }

    override fun getHeadInterceptLength(): Int {
        return 1
    }

    override fun getTailInterceptChar(): CharSequence {
        return "\r\n)"
    }

    override fun getTailInterceptLength(): Int {
        return 3
    }

    override fun bodyParse(bodyArray: ByteArray): String {
        val bodyString = String(bodyArray)
        val split = bodyString.split(SPLIT_HEAD)
        val cmd = split[0]
        val data = "{" + split[1].replace(' ', ',')
            .replace('=', ':') + "}"
        val jsonObject = JSONObject()
        jsonObject.put("cmd", cmd)
        jsonObject.put("data", JSONObject(data))
        return jsonObject.toString()
    }

}