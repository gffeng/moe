package com.minivision.moe.core

import com.minivision.moe.interceptor.MoeInterceptor
import com.minivision.moe.meta.HeartBeatConfig
import com.minivision.moe.meta.MoeConfig
import com.minivision.moe.meta.MoeType
import com.minivision.moe.meta.ThreadStrategy
import com.minivision.moe.post.AsyncPoster
import com.minivision.moe.post.IPoster
import com.minivision.moe.post.SyncPoster
import com.minivision.parameter.util.LogUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.Closeable
import java.net.InetSocketAddress
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * 客户端基类
 * @author gf
 * @date 2021/4/22
 */
abstract class MoeClient(val config: MoeConfig) : Closeable {
    private var mHost: String = ""
    private var mPort: Int = 1080
    protected var connectJob: Job? = null
    private var heartJob: Job? = null
    private var heartBeatConfig: HeartBeatConfig? = null
    var mAddress: InetSocketAddress
    var mTimeOut: Int = 30000
    var mUrl: String = ""
    var moeInterceptor: MoeInterceptor? = null
    private var mIPoster: IPoster? = null
    protected var moeListener: BaseMoeListener? = null

    /**
     * 监听连接状态
     * 成功：开启心跳
     * 失败：关闭心跳，尝试重连
     */
    var connectStatus: Boolean by Delegates.observable(false) { _: KProperty<*>, _: Boolean, new: Boolean ->
        if (new) {
            initHeartBeat()
        } else {
            cancelHeartBeat()
            if (this is TcpSocket) {
                connect(config.mTimeout.toLong())
            }
        }
    }


    /**
     * 初始化
     */
    init {
        checkConfig()
        mAddress = InetSocketAddress(mHost, mPort)
        initPoster()
    }


    /**
     * 初始化消息发送器[ThreadStrategy]
     */
    private fun initPoster() {
        mIPoster = if (config.mThreadStrategy == ThreadStrategy.ASYNC) AsyncPoster(this, mExecutor)
        else SyncPoster(this, mExecutor)
    }

    /**
     * 检查IP地址是否正确
     */
    fun checkConfig() {
        if (config.moeType == MoeType.TCP || config.moeType == MoeType.UDP) {
            if (config.mIp.isNullOrEmpty()) throw NullPointerException("MoeConfig ip must not null")
            this.mHost = config.mIp
            config.mPort?.let {
                this.mPort = it
            }
        } else {
            if (config.mUrl.isNullOrEmpty()) throw NullPointerException("MoeConfig url must not null")
            this.mUrl = config.mUrl
        }
        this.mTimeOut = config.mTimeout

    }


    /**
     * 开启连接
     */
    @Synchronized
    fun connect() {
        connect(0)
    }

    /**
     * 开启连接
     * @param time 延迟开启时间
     */
    abstract fun connect(time: Long)

    /**
     * 是否连接成功
     * @return True/False
     */
    abstract fun isConnected(): Boolean

    /**
     * 发送数据
     * @param data 字节数组
     */
    abstract fun send(data: ByteArray)

    /**
     * 发送数据
     * @param data 字符
     */
    fun sendData(data: String) {
        var temp: ByteArray
        if (moeInterceptor != null) {
            temp = moeInterceptor!!.sendIntercept(data)
        } else {
            temp = data.toByteArray()
        }
        mIPoster?.enqueue(temp)
    }


    /**
     * 设置心跳
     * @param heartBeatConfig 心跳配置[HeartBeatConfig]
     * @return MoeClient
     */
    fun setHeartBeat(heartBeatConfig: HeartBeatConfig): MoeClient {
        this.heartBeatConfig = heartBeatConfig
        return this
    }

    /**
     * 设置拦截器
     * @param moeInterceptor 消息拦截器[MoeInterceptor]
     * @return MoeClient
     */
    fun setInterceptor(moeInterceptor: MoeInterceptor): MoeClient {
        this.moeInterceptor = moeInterceptor
        return this
    }

    /**
     * 设置监听
     * @param moeListener 监听[MoeListener]
     * @return MoeClient
     */
    fun setListener(moeListener: BaseMoeListener): MoeClient {
        this.moeListener = moeListener
        return this
    }

    /**
     * 初始化心跳
     * 若设置了心跳配置则开启心跳
     */
    private fun initHeartBeat() {
        heartBeatConfig?.apply {
            LogUtil.i(TAG, "initHeartBeat")
            heartJob = scope.launch {
                while (isActive) {
                    delay(interval)
                    if (isConnected()) {
                        sendData(data.parse())
                    }
                }
            }
        }
    }


    /**
     * 取消心跳
     */
    private fun cancelHeartBeat() {
        heartJob?.run {
            cancel()
            LogUtil.i(TAG, "cancelHeartBeat")
            heartJob = null
        }
    }


    /**
     * close
     */
    @Synchronized
    override fun close() {
        cancelHeartBeat()
    }

}