package com.minivision.moe.interceptor

import com.minivision.moe.core.LOG_D
import com.minivision.moe.util.MoeUtils
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

/**
 *广联达项目陇明公使用的拦截器
 * 消息格式:
 *
 *  * 起始位置	    长度	内容说明	    标记	    类型	    备注
 *  0	        1	    开始标记	    Header	    Byte	    0x01
 *  1	        4	    长度 LEN	Length	    Uint	    内容的长度
 *  5	        4	    分包顺序索引	PartIndex	Uint	    0x00
 *  9	        4	    分包总数	    PartCount	Uint	    0x00
 *  13	        1	    版本	    Version	    Byte	    默认0x03
 *  14	        2	    命令	    Command	    Ushort	    0x10
 *  16	        16	    会话标识	    SessionID	Byte[16]	通讯唯一标识，每一个tcp连接由设备随机生成
 *  32	        LEN	    内容	    Content	    Byte[LEN]	数据包内容
 *  32 + LEN	1	    状态	    Flag	    Byte	    0x0为成功，0x01为失败
 *  33 + LEN	1	    结束标记	    Tail	    Byte	    0x01
 *
 *
 * @author gf
 * @date 2021/4/23
 */
class LMGInterceptor : CommonTcpInterceptor() {
    override fun getHeadInterceptChar(): CharSequence {
        return String(MoeUtils.hexToByteArray("01"))
    }

    override fun getHeadInterceptLength(): Int {
        return 1
    }

    override fun getTailInterceptChar(): CharSequence {
        return String(MoeUtils.hexToByteArray("01"))
    }

    override fun getTailInterceptLength(): Int {
        return 1
    }

    @Throws(IOException::class)
    override fun receiveIntercept(ist: InputStream): ByteArray? {
        try {
            val headerLength = getHeadInterceptLength()
            val headBuf = ByteBuffer.allocate(headerLength)
            readFromChannel(ist, headBuf)
            //头部相同
            if (String(headBuf.array()) == getHeadInterceptChar()) {
                //内容长度
                val lengthBuf = ByteBuffer.allocate(4)
                readFromChannel(ist, lengthBuf)
                val bodyLength = MoeUtils.byteArrayToInt(lengthBuf.array())
                //分包顺序索引
                val partIndexBuf = ByteBuffer.allocate(4)
                readFromChannel(ist, partIndexBuf)
                //分包总数
                val partCountBuf = ByteBuffer.allocate(4)
                readFromChannel(ist, partCountBuf)
                //版本
                val versionBuf = ByteBuffer.allocate(1)
                readFromChannel(ist, versionBuf)
                //命令
                val cmdBuf = ByteBuffer.allocate(2)
                readFromChannel(ist, cmdBuf)
//                LOG_D("cmd:${MoeUtils.byteArrayToInt(cmdBuf.array())}")
                //ReqId
                val reqBuf = ByteBuffer.allocate(16)
                readFromChannel(ist, reqBuf)
//                LOG_D("reqId:${MoeUtils.byteToHexString(reqBuf.array())}")
                //BODY
                val bodyBuf = ByteBuffer.allocate(bodyLength)
                readFromChannel(ist, bodyBuf)
                //status
                val statusBuf = ByteBuffer.allocate(1)
                readFromChannel(ist, statusBuf)
//                LOG_D("status:${MoeUtils.byteArrayToInt(statusBuf.array())}")
                //tail
                val tailBuf = ByteBuffer.allocate(1)
                readFromChannel(ist, tailBuf)

                //尾部相同
                if (String(tailBuf.array()) == getTailInterceptChar()) {
                    val buf = ByteBuffer.allocate(34 + bodyLength)
                    buf.put(headBuf.array())
                    buf.put(lengthBuf.array())
                    buf.put(partIndexBuf.array())
                    buf.put(partCountBuf.array())
                    buf.put(versionBuf.array())
                    buf.put(cmdBuf.array())
                    buf.put(reqBuf.array())
                    buf.put(bodyBuf.array())
                    buf.put(statusBuf.array())
                    buf.put(tailBuf.array())
                    return buf.array()
                }
            }
            return null
        } catch (e: Exception) {
            throw e
        }
    }


    /**
     * 从管道中读取消息头
     */
    @Throws(IOException::class)
    private fun readFromChannel(
            ist: InputStream,
            buf: ByteBuffer
    ) {
        val length = buf.capacity()
        var remainder = length
        while (remainder > 0) {
            val bytes = ByteArray(remainder)
            val value = ist.read(bytes)
            if (value < remainder) {
                throw java.lang.Exception(
                        "read buffer is wrong, read length ${length - remainder} < body length $length")
            }
            remainder -= value
            buf.put(bytes, 0, value)
        }
    }

    override fun bodyParse(bodyArray: ByteArray): String {
        /**
         * 解析
         */
        val lengthBuf = ByteBuffer.allocate(4)
        lengthBuf.put(bodyArray, 1, 4)
        val bodyLength = MoeUtils.byteArrayToInt(lengthBuf.array())

        val cmdBuf = ByteBuffer.allocate(2)
        cmdBuf.put(bodyArray, 14, 2)
        val cmd = MoeUtils.byteArrayToInt(cmdBuf.array())

        val reqIdBuf = ByteBuffer.allocate(16)
        reqIdBuf.put(bodyArray, 16, 16)
        val reqId = String(reqIdBuf.array())

        val bodyBuf = ByteBuffer.allocate(bodyLength)
        bodyBuf.put(bodyArray, 32, bodyLength)
        val body = MoeUtils.byteToHexString(bodyBuf.array())

        val statusBuf = ByteBuffer.allocate(1)
        statusBuf.put(bodyArray, 32 + bodyLength, 1)
        val statusHex = MoeUtils.byteToHexString(statusBuf.array())
        val status = MoeUtils.hexToInt(statusHex)

        val jsonObject = JSONObject()
        jsonObject.put("cmd", cmd)
        jsonObject.put("reqId", reqId)
        jsonObject.put("data", body)
        jsonObject.put("status", status)

        return jsonObject.toString()
    }

    override fun sendIntercept(text: String): ByteArray {
        LOG_D("send message:$text")
        return MoeUtils.hexToByteArray(text)
    }
}