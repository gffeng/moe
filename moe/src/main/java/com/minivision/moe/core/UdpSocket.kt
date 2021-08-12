package com.minivision.moe.core

import com.minivision.moe.interceptor.UdpInterceptor
import com.minivision.moe.meta.MoeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket

/**
 * UDP协议客户端
 * @author gf
 * @date 2021/4/20
 */
class UdpSocket(config: MoeConfig) : MoeClient(config) {
    private var socket: DatagramSocket? = null

    /**
     * 开启连接
     * @param time 延迟开启时间
     */
    override fun connect(time: Long) {
        checkConfig()
        if (connectJob?.isActive == true) {
            ULOG_I("udp connectJob isActive ")
            return
        }

        connectJob = scope.launch {
            delay(time)
            if (!isConnected()) {
                ULOG_I("udp createClient ")
                config.run {
                    createClient()
                }
            }
            read()
        }
    }

    /**
     * 创建客户端
     * 由于UDP协程没用进行长连接，所以不需要重试
     */
    private suspend fun createClient() {
        withContext(Dispatchers.IO) {
            try {
                socket = DatagramSocket(LOCAL_PORT)
                connectStatus = true
                getListener()?.onConnected()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 是否连接成功
     * UDP协议不进行长连接，此处使用客户端是否存在判断
     *
     * @return True/False
     */
    override fun isConnected(): Boolean {
        return socket != null
    }

    /**
     * 接收UDP packet[DatagramPacket]
     */
    private fun read() {
        try {
            while (isConnected()) {
                val temp = ByteArray(LIMIT)
                val datagramPacket = DatagramPacket(temp, temp.size)
                socket!!.receive(datagramPacket)
                val data = datagramPacket.data
                val length = datagramPacket.length
                val byteArray = data.copyOfRange(0, length)
                ULOG_D("origin msg:${String(byteArray)}")
                if (moeInterceptor != null && moeInterceptor is UdpInterceptor) {
                    (moeInterceptor as UdpInterceptor).receiveIntercept(byteArray)?.let {
                        val body = moeInterceptor!!.bodyParse(it)
                        ULOG_I("arrive message:$body")
                        getListener()?.onResponse(body)
                    }
                } else {
                    val body = String(byteArray)
                    ULOG_I("arrive message:$body")
                    getListener()?.onResponse(body)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ULOG_I("udp receive message error")
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
                    ULOG_I("send message:${String(data)}")
                    val datagramPacket = DatagramPacket(data, data.size, mAddress)
                    socket!!.send(datagramPacket)
                } else {
                    ULOG_I("cannot send message, udp disconnect")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ULOG_I("udp send message error")
            }
        }
    }

    /**
     * UDP关闭客户端
     */
    @Synchronized
    override fun close() {
        super.close()
        connectJob?.run {
            cancel()
            TLOG_I("udp connect cancel")
            connectJob = null
        }
        socket?.run {
            close()
            socket = null
            TLOG_I("udp close")
            getListener()?.onDisConnected()
        }
        connectStatus = false
    }

    private fun getListener(): MoeListener? {
        return moeListener?.let {
            it as MoeListener
        }
    }

    companion object {
        const val LOCAL_PORT = 9999
    }
}