package com.minivision.moe.interceptor

/**
 *
 * @author gf
 * @date 2021/4/23
 */
interface UdpInterceptor :MoeInterceptor{

    /**
     * 消息接收拦截
     * 消息体一般分为 HEAD+BODY+TAIL
     *
     * @param byteArray 接收到的字节数组
     * @return 处理后的字节数组
     */
    fun receiveIntercept(byteArray: ByteArray): ByteArray?
}