package com.minivision.moe.interceptor

import java.io.InputStream

/**
 *
 * @author gf
 * @date 2021/4/23
 */
interface TcpInterceptor : MoeInterceptor {

    /**
     * 消息接收拦截
     * 消息体一般分为 HEAD+BODY+TAIL
     *
     * @param ist 流
     * @return 处理后的字节数组
     */
    fun receiveIntercept(ist: InputStream): ByteArray?
}