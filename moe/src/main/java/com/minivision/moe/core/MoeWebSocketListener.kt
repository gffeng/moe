package com.minivision.moe.core

import okio.ByteString

/**
 *
 * @author gf
 * @date 2021/4/21
 */
interface MoeWebSocketListener : BaseMoeListener {

    fun onResponse(data: String?, byteString: ByteString?)
}