package com.minivision.moe.post

interface IPoster {
    fun enqueue(data: ByteArray)
}