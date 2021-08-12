package com.minivision.moe.meta

import com.minivision.moe.interceptor.LMGInterceptor
import com.minivision.moe.util.MoeUtils

/**
 * 陇明公解析后消息体
 * [LMGInterceptor]
 *
 * 起始位置	    长度	内容说明	    标记	    类型	    备注
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
 * @author gf
 * @date 2021/5/17
 */
class LMGEntity(var cmd: Int, var data: String) : ISendEntity {

    private val req = MoeUtils.getGUID()

    /**
     * 将JSON格式转换成广联达格式
     */
    override fun parse(): String {
        val sb = StringBuffer()
        sb.append("01 ")
        sb.append(MoeUtils.intToLHex(data.split(" ").size - 1, 4))
        sb.append("00 00 00 00 00 00 00 00 03 ")
        sb.append(MoeUtils.shortToHex(cmd))
        sb.append(MoeUtils.string2Hex(requestId(), 16))
        sb.append(data)
        sb.append("01 ")
        sb.append("01 ")
        return sb.toString()
    }

    override fun requestId(): String {
        return req
    }

    override fun randomRequestId() {
    }


    override fun body(): Any? {
        return data
    }
}