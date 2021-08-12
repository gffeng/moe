package com.minivision.moe

import com.minivision.moe.core.MoeClient
import com.minivision.moe.core.TcpSocket
import com.minivision.moe.core.UdpSocket
import com.minivision.moe.core.WebSocketClient
import com.minivision.moe.meta.MoeConfig
import com.minivision.moe.meta.MoeType

/**
 * 创建 [MoeClient]
 * 包括 [TcpSocket] [UdpSocket]
 * @author gf
 * @date 2021/4/22
 */
class MoeFactory {
    companion object {
        fun create(config: MoeConfig): MoeClient {
            return when (config.moeType) {
                MoeType.TCP -> {
                    //TCP类型
                    TcpSocket(config)
                }
                MoeType.UDP -> {
                    //UDP类型
                    UdpSocket(config)
                }
                MoeType.WebSocket->{
                    //websocket类型
                    WebSocketClient(config)
                }
                else -> TcpSocket(config)
            }
        }
    }
}