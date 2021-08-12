package com.minivision.moe.interceptor

/**
 *
 * @author gf
 * @date 2021/4/23
 */
interface MoeInterceptor {
    /**
     * 消息体头部
     * 例： "("
     * @return 消息体头部字符
     */
    fun getHeadInterceptChar(): CharSequence

    /**
     * 消息体头部的长度,
     * 例： "("=1
     *      "\r\n"=2
     * @return 消息体头的长度
     */
    fun getHeadInterceptLength(): Int

    /**
     * 消息体尾部
     * 例： ")"
     * @return 消息体尾部字符
     */
    fun getTailInterceptChar(): CharSequence

    /**
     * 消息体尾部的长度,
     * 例： ")"=1
     *      "\r\n)"=3
     * @return 消息体头的长度
     */
    fun getTailInterceptLength(): Int

//    /**
//     * 消息接收拦截
//     * 消息体一般分为 HEAD+BODY+TAIL
//     *
//     * @param byteArray 接收到的字节数组
//     * @return 处理后的字节数组
//     */
//    fun receiveIntercept(byteArray: ByteArray): ByteArray?

    /**
     * 消息体Body解析

     * 将通过[receiveIntercept]返回的[ByteArray]按照特定方式转成字符串
     *
     * @param bodyArray 消息Body的字节数组
     * @return 转换后的字符串
     */
    fun bodyParse(bodyArray: ByteArray): String

    /**
     * 消息发送拦截
     * 发送消息可以拦截并转换成特定格式
     * @param text 发送的消息
     * @return 转换后的消息
     */
    fun sendIntercept(text: String): ByteArray
}