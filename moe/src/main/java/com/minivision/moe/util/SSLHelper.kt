package com.minivision.moe.util

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 *
 * @author gf
 * @date 2020/8/18
 */
object SSLHelper {

    /**
     * 获取这个SSLSocketFactory
     *
     * @return SSLSocketFactory
     */
    fun getSSLSocketFactory(trustManager:Array<TrustManager>?): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustManager, SecureRandom())
        return sslContext.socketFactory
    }

    /**
     * 获取TrustManager
     *
     * @return TrustManager[]
     */
    fun getTrustManager(): Array<TrustManager> {
        return arrayOf(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
        )
    }

    /**
     * 获取HostnameVerifier
     *
     * @return HostnameVerifier
     */
    fun getHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { s, sslSession -> true }
    }
}