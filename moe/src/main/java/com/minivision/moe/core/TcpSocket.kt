package com.minivision.moe.core

import com.minivision.moe.interceptor.TcpInterceptor
import com.minivision.moe.meta.MoeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * TCP协议客户端
 * @author gf
 * @date 2021/4/20
 */
class TcpSocket(config: MoeConfig) : MoeClient(config) {
    private var ost: OutputStream? = null
    private var ist: InputStream? = null
    private var socket: Socket? = null

    /**
     * 开启连接
     * @param time 延迟开启时间
     */
    override fun connect(time: Long) {
        checkConfig()
        if (connectJob?.isActive == true) {
            TLOG_I("tcp connectJob isActive ")
            return
        }
        connectJob = scope.launch {
            delay(time)
            if (!isConnected()) {
                TLOG_I("tcp createClient ")
                createClient()
            }
            read()
        }
    }

    /**
     * 是否连接成功
     * @return True/False
     */
    override fun isConnected(): Boolean {
        return socket?.run { isConnected } ?: false
    }

    /**
     * 创建客户端
     * 创建失败则进行重试
     */
    private suspend fun createClient() {
        withContext(Dispatchers.IO) {
            try {
                Socket().also {
                    socket = it
                    it.connect(mAddress, mTimeOut)
                    it.keepAlive = true
                    ost = it.getOutputStream()
                    ist = it.getInputStream()
                    connectStatus = true
                    getListener()?.onConnected()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                closeAndReConnect()
            }
        }
    }

    /**
     * 读取流
     */
    private fun read() {
        try {
            while (isConnected()) {
                if (moeInterceptor != null && moeInterceptor is TcpInterceptor) {
                    (moeInterceptor as TcpInterceptor).receiveIntercept(ist!!)?.let {
                        val body = moeInterceptor!!.bodyParse(it)
                        TLOG_I("arrive message:$body")
                        getListener()?.onResponse(body)
                    }
                } else {
                    val temp = ByteArray(LIMIT)
                    val length = ist!!.read(temp)
                    if (length <= 0) {
                        break
                    }
                    val body = String(temp)
                    TLOG_I("arrive message:${String(temp)}")
                    getListener()?.onResponse(body)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            closeAndReConnect()
            TLOG_I("tcp receive message error")
        }
    }

    /**
     * 发送数据
     * @param data 字节数组
     */
    override fun send(data: ByteArray) {
        scope.launch {
            try {
                if (isConnected()) {
                    //                    TLOG_I("send message:${String(data)}")
                    ost!!.write(data)
                    ost!!.flush()
                } else {
                    TLOG_I("cannot send message,tcp disconnect")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                closeAndReConnect()
                TLOG_I("tcp send message error")
            }
        }
    }

    /**
     * 关闭客户端并进行重连
     * 通过监听[connectStatus]状态进行重连
     */
    private fun closeAndReConnect() {
        close()
        connectStatus = false
    }

    /**
     * TCP关闭客户端
     */
    @Synchronized
    override fun close() {
        super.close()
        connectJob?.run {
            cancel()
            TLOG_I("tcp connectJob cancel")
            connectJob = null
        }
        socket?.run {
            close()
            socket = null
            TLOG_I("tcp close")
            getListener()?.onDisConnected()
        }
    }

    private fun getListener(): MoeListener? {
        return moeListener?.let {
            it as MoeListener
        }
    }
}