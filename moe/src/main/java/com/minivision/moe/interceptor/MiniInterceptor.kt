package com.minivision.moe.interceptor

import com.minivision.moe.core.LOG_D
import com.minivision.moe.meta.DataFormat
import com.minivision.moe.util.MoeUtils
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 小视Socket消息格式的解析拦截
 * 消息格式为：
 * 报文前8位：Json数据转字节数组长度（不够八位前面补0）
 * 报文体：Json数据
 * 例：
 *  原消息：00000084{"head":{"code":103,"requestId":"39ed834b-43f3-445b-a562-75ce3d4f8a54","resCode":1}}
 *  拦截后消息：{"head":{"code":103,"requestId":"39ed834b-43f3-445b-a562-75ce3d4f8a54","resCode":1}}
 * @author gf
 * @date 2021/4/23
 */
class MiniInterceptor : CommonTcpInterceptor() {

    @Throws(IOException::class)
    override fun receiveIntercept(ist: InputStream): ByteArray? {
        val headerLength = getHeadInterceptLength()
        val headBuf = ByteBuffer.allocate(headerLength)
        headBuf.order(ByteOrder.BIG_ENDIAN)
        if (mRemainingBuf != null) {
            mRemainingBuf?.apply {
                flip()
                val length = Math.min(remaining(), headerLength)
                headBuf.put(array(), 0, length)
                LOG_D("length:$length, headerLength:$headerLength")
                if (length < headerLength) {
                    mRemainingBuf = null
                    readHeaderFromChannel(ist, headBuf, headerLength - length)
                } else {
                    position(headerLength)
                }
            }
        } else {
            readHeaderFromChannel(ist, headBuf, headBuf.capacity())
        }
        LOG_D("read head: " + MoeUtils.byteToHexString(headBuf.array()))
        val bodyLength = getBodyLength(headBuf.array(), headerLength)
        LOG_D("need read body length: $bodyLength")
        when {
            bodyLength > 0 -> {
                val bodyBuffer = ByteBuffer.allocate(bodyLength)
                bodyBuffer.order(ByteOrder.BIG_ENDIAN)
                mRemainingBuf?.apply {
                    val bodyStartPosition = position()
                    val length = Math.min(remaining(), bodyLength)
                    bodyBuffer.put(array(), bodyStartPosition, length)
                    position(bodyStartPosition + length)
                    if (length == bodyLength) {
                        if (remaining() > 0) { //有数据剩余，继续读
                            val temp = ByteBuffer.allocate(remaining())
                            temp.order(ByteOrder.BIG_ENDIAN)
                            temp.put(array(), position(), remaining())
                            mRemainingBuf = temp
                        } else { //无剩余缓存数据
                            mRemainingBuf = null
                        }
                    } else { //there are no data left in buffer and some data pieces in channel
                        mRemainingBuf = null
                    }
                }
                readBodyFromChannel(ist, bodyBuffer)
                return bodyBuffer.array()
            }
            bodyLength == 0 -> {
                mRemainingBuf?.apply {
                    if (hasRemaining()) {
                        val temp = ByteBuffer.allocate(remaining())
                        temp.order(ByteOrder.BIG_ENDIAN)
                        temp.put(array(), position(), remaining())
                        mRemainingBuf = temp
                    } else {
                        mRemainingBuf = null
                    }
                }
                return null
            }
            else -> throw Exception(
                    "read body is wrong,this socket input stream is end of file read $bodyLength ,that mean this socket is disconnected by server")
        }
    }

    override fun bodyParse(bodyArray: ByteArray): String {
        return String(bodyArray)
    }

    /**
     * 从管道中读取消息头
     */
    @Throws(IOException::class)
    private fun readHeaderFromChannel(
            ist: InputStream,
            headBuf: ByteBuffer,
            readLength: Int
    ) {
        for (i in 0 until readLength) {
            val bytes = ByteArray(1)
            val value = ist.read(bytes)
            if (value == -1) {
                throw java.lang.Exception(
                        "read head is wrong, this socket input stream is end of file read $value ,that mean this socket is disconnected by server")
            }
            headBuf.put(bytes)
        }
    }

    /**
     * 从管道中读取消息体
     */
    @Throws(IOException::class)
    private fun readBodyFromChannel(ist: InputStream, byteBuffer: ByteBuffer) {
        while (byteBuffer.hasRemaining()) {
            try {
                val bufArray = ByteArray(100)
                val len = ist.read(bufArray)
                if (len == -1) {
                    break
                }
                val remaining = byteBuffer.remaining()
                if (len > remaining) {
                    byteBuffer.put(bufArray, 0, remaining)
                    mRemainingBuf = ByteBuffer.allocate(len - remaining)
                    mRemainingBuf?.order(ByteOrder.BIG_ENDIAN)
                    mRemainingBuf?.put(bufArray, remaining, len - remaining)
                } else {
                    byteBuffer.put(bufArray, 0, len)
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    /**
     * 获取消息体长度
     */
    @Throws(NumberFormatException::class)
    private fun getBodyLength(header: ByteArray?, headerLength: Int): Int {
        if (header == null || header.size < headerLength) {
            return 0
        }
        val bb = ByteBuffer.wrap(header)
        bb.order(ByteOrder.BIG_ENDIAN)
        return if (getHeadInterceptChar().isNotEmpty()) String(bb.array()).toInt() else (bb.int - headerLength)
    }

    /**
     * 消息头格式[DataFormat.HEAD_FORMAT]
     * "%0${HEAD_SIZE}d"
     */
    override fun getHeadInterceptChar(): CharSequence {
        return DataFormat.HEAD_FORMAT
    }

    override fun getHeadInterceptLength(): Int {
        return 8
    }

    override fun getTailInterceptChar(): CharSequence {
        return ""
    }

    override fun getTailInterceptLength(): Int {
        return 0
    }

    override fun sendIntercept(text: String): ByteArray {
        val temp = String.format(getHeadInterceptChar().toString(), text.length) + text
        return temp.toByteArray()
    }
}