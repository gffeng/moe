package com.minivision.moe.interceptor

import java.nio.ByteBuffer

/**
 *通用的拦截器
 *拦截头和尾
 *例 ：
 *  原消息：（123）
 *  头：'('
 *  尾：')'
 *  拦截后消息：123
 *
 * @author gf
 * @date 2021/4/23
 */
open class CommonInterceptor : MoeInterceptor {
    var mRemainingBuf: ByteBuffer? = null
    override fun getHeadInterceptChar(): CharSequence {
        return ""
    }

    override fun getHeadInterceptLength(): Int {
        return 0
    }

    override fun getTailInterceptChar(): CharSequence {
        return ""
    }

    override fun getTailInterceptLength(): Int {
        return 0
    }

    override fun bodyParse(bodyArray: ByteArray): String {
        return String(bodyArray)
    }

    override fun sendIntercept(text: String): ByteArray {
        return text.toByteArray()
    }
}