package com.minivision.moe.meta

import java.nio.charset.Charset


class MoeConfig(
    @MoeType val moeType: String,
    val mIp: String?,
    val mPort: Int?,
    val mUrl: String?,
    val mTimeout: Int = 30000,
    val mCharset: Charset = Charsets.UTF_8,
    val mThreadStrategy: Int?
) {

    private constructor(builder: Builder) : this(
        builder.moeType,
        builder.mIp, builder.mPort, builder.mUrl,
        builder.mTimeout, builder.mCharset,
        builder.mThreadStrategy
    )

    class Builder {
        @MoeType
        lateinit var moeType: String
            private set

        var mIp: String? = null
            private set

        var mPort: Int? = null
            private set

        var mUrl: String? = null
            private set

        var mTimeout: Int = 30000
            private set

        var mCharset: Charset = Charsets.UTF_8
            private set

        var mThreadStrategy: Int? = ThreadStrategy.ASYNC
            private set

        fun setMoeType(@MoeType moeType: String) = apply {
            this.moeType = moeType
        }

        fun setIp(ip: String) = apply { this.mIp = ip }

        fun setPort(port: Int) = apply { this.mPort = port }

        fun setUrl(url: String) = apply { this.mUrl = url }

        fun setTimeout(timeout: Int) = apply { this.mTimeout = timeout }

        fun setCharset(charset: Charset) = apply { this.mCharset = charset }

        fun setThreadStrategy(threadStrategy: Int) = apply { this.mThreadStrategy = threadStrategy }

        fun build() = MoeConfig(this)
    }
}