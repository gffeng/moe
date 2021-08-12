package com.minivision.moe.core

import com.minivision.moe.meta.MoeConfig
import com.minivision.moe.util.SSLHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

/**
 * Web协议客户端
 * @author gf
 * @date 2021/4/20
 */
class WebSocketClient(config: MoeConfig) : MoeClient(config) {
    private var okhttpClient: OkHttpClient
    private val listener: SocketListener = SocketListener()
    private var webSocket: WebSocket? = null

    init {
        val trustManager = SSLHelper.getTrustManager()
        okhttpClient = OkHttpClient.Builder()
            .sslSocketFactory(
                SSLHelper.getSSLSocketFactory(trustManager),
                trustManager[0] as X509TrustManager
            )
            .hostnameVerifier(SSLHelper.getHostnameVerifier())
            .readTimeout(mTimeOut.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(mTimeOut.toLong(), TimeUnit.MILLISECONDS)
            .build()
    }

    /**
     * 开启连接
     * @param time 延迟开启时间
     */
    override fun connect(time: Long) {
        checkConfig()
        connectJob = scope.launch {
            delay(time)
            if (!isConnected()) {
                WLOG_I(" connect $mUrl")
                createClient()
            }
        }
    }

    /**
     * 是否连接成功
     * @return True/False
     */
    override fun isConnected(): Boolean {
        return connectStatus
    }

    /**
     * 创建客户端
     * 创建失败则进行重试
     */
    private suspend fun createClient() {
        withContext(Dispatchers.IO) {
            val request: Request = Request.Builder()
                .url(mUrl)
                .build()
            webSocket = okhttpClient.newWebSocket(request, listener)
        }
    }

    /**
     * 发送数据
     * @param data 字节数组
     */
    override fun send(data: ByteArray) {
        scope.launch {
            if (isConnected()) {
                val string = String(data)
                WLOG_D("send :$string")
                webSocket?.send(string)
            } else {
                WLOG_I("cannot send message,WebSocket disconnect")
            }

        }
    }


    /**
     * TCP关闭客户端
     */
    @Synchronized
    override fun close() {
        super.close()
        connectJob?.run {
            cancel()
            WLOG_I("WebSocket connectJob cancel")
            connectJob = null
        }
        webSocket?.run {
            close(1000, null)
            webSocket = null
            WLOG_I("WebSocket close")
        }
    }


    private fun getListener(): MoeWebSocketListener? {
        return moeListener?.let {
            it as MoeWebSocketListener
        }
    }


    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            WLOG_I("onConnect")
            connectStatus = true
            getListener()?.onConnected()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            WLOG_I("onFailure:${t.message.toString()}")
            connectStatus = false
            getListener()?.onDisConnected()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            WLOG_I("onClosing")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            WLOG_D("onMessage: $text")
            getListener()?.onResponse(text, null)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            WLOG_D("onMessage: bytes:$bytes")
            getListener()?.onResponse(null, bytes)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            WLOG_I("[WebSocket]: onClosed: $reason")
            connectStatus = false
            getListener()?.onDisConnected()
        }
    }
}