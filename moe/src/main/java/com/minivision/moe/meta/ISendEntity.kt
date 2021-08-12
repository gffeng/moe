package com.minivision.moe.meta

/**
 * @author lyuyou
 * @date 2019/3/1
 */
interface ISendEntity {
    fun parse(): String
    fun requestId(): String
    fun randomRequestId()
    fun body() : Any?
}