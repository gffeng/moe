package com.minivision.moe.core

/**
 *
 * @author gf
 * @date 2021/4/21
 */
interface MoeListener : BaseMoeListener {

    fun onResponse(data: String)
}