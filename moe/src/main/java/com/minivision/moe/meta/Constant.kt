
package com.minivision.moe.meta

/**
 * @author: Est <codeest.dev@gmail.com>
 * @date: 2017/7/8
 * @description:
 */
object SocketState {

    const val OPEN = 0x00

    const val CONNECTING = 0x01

    const val DISCONNECT = 0x02
}

object ThreadStrategy {

    const val SYNC = 0x00

    const val ASYNC = 0x01
}

object DataFormat {
    const val HEAD_SIZE = 8

    const val HEAD_FORMAT = "%0${HEAD_SIZE}d"
}