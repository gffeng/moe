package com.minivision.moe.interceptor

import com.minivision.moe.core.LIMIT
import com.minivision.moe.core.LOG_I
import java.io.IOException
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
open class CommonUdpInterceptor : CommonInterceptor(), UdpInterceptor {

    /**
     *  消息发送大体分为二种情况
     *  一： 【*****    】 发送数据小于[LIMIT]，一次分片既可以获取完整数据
     *  二： 【*********】【*********】【****   】 分片大于[LIMIT],数据需要分多次发送，所以需要多次接收，
     *        其中还要考虑数据包丢失问题，有可能多次接收的不是同一批数据等
     */
    @Throws(IOException::class)
    override fun receiveIntercept(byteArray: ByteArray): ByteArray? {
        try {
            val len = byteArray.size
            val headLength = getHeadInterceptLength()
            val tailLength = getTailInterceptLength()

            val headBuf = ByteBuffer.allocate(headLength)
            headBuf.put(byteArray, 0, headLength)
            if (String(headBuf.array()) == getHeadInterceptChar()) {
                //头正确,清空Remain
                mRemainingBuf = null
                val tailBuf = ByteBuffer.allocate(tailLength)
                tailBuf.put(byteArray, len - tailLength, tailLength)
                if (String(tailBuf.array()) == getTailInterceptChar()) {
                    //尾正确，数据读取完毕
                    val bodyBuf = ByteBuffer.allocate(len - headLength - tailLength)
                    bodyBuf.put(byteArray, headLength, len - headLength - tailLength)
                    return bodyBuf.array()
                } else {
                    //尾错误
                    if (len == LIMIT) {
                        //读满1024，数据需要保存
                        mRemainingBuf = ByteBuffer.allocate(len - headLength)
                        mRemainingBuf?.put(byteArray, headLength, len - headLength)
                    } else {
                        //未读满，报文异常
                        LOG_I("报文结尾解析错误:${String(tailBuf.array(), 0, tailLength)}")
                        mRemainingBuf = null
                    }
                }
            } else {
                //头错误，接着上次结尾继续
                val tailBuf = ByteBuffer.allocate(tailLength)
                tailBuf.put(byteArray, len - tailLength, tailLength)
                if (String(tailBuf.array()) == getTailInterceptChar()) {
                    //尾正确，数据读取完毕，读取Remain数据+此时数据
                    if (mRemainingBuf != null) {
                        return mRemainingBuf?.run {
                            val bodyBuf = ByteBuffer.allocate(len - tailLength + capacity())
                            bodyBuf.put(array(), 0, capacity())
                            bodyBuf.put(byteArray, 0, len - tailLength)
                            mRemainingBuf = null
                            bodyBuf.array()
                        }
                    } else {
                        LOG_I("报文解析错误:${String(byteArray, 0, len)}")
                        mRemainingBuf = null
                    }
                } else {
                    //尾错误
                    if (len == LIMIT) {
                        //(头错误+尾错误)，读满1024
                        if (mRemainingBuf != null) {
                            //remain存在，则继续保存
                            mRemainingBuf?.apply {
                                val temp = ByteBuffer.allocate(LIMIT + capacity())
                                temp.put(array(), 0, capacity())
                                temp.put(byteArray, 0, LIMIT)
                                mRemainingBuf = temp
                            }
                        } else {
                            //(头错误+尾错误) remain为空，异常数据
                            mRemainingBuf = null
                        }
                    } else {
                        //未读满，报文异常
                        LOG_I("报文结尾解析错误:${String(tailBuf.array(), 0, tailLength)}")
                        mRemainingBuf = null
                    }
                }
            }
            return null
        } catch (e: Exception) {
            throw e
        }
    }
}