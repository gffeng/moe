package com.minivision.moe.entiy

import com.minivision.moe.meta.ISendEntity

/**
 *
 * @author gf
 * @date 2021/4/22
 */

class MiniHeart() : ISendEntity {
    override fun parse(): String {
        return "{\"head\":{\"code\":103,\"requestId\":\"39ed834b-43f3-445b-a562-75ce3d4f8a54\",\"resCode\":1}}"
    }

    override fun requestId(): String {
        return ""
    }

    override fun randomRequestId() {
    }

    override fun body(): Any? {
        return null
    }
}




