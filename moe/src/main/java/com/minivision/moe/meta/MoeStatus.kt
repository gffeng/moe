package com.minivision.moe.meta

import kotlin.annotation.AnnotationRetention.SOURCE

/**
 *
 * @author gf
 * @date 2021/6/22
 */
@Retention(SOURCE)
annotation class MoeStatus {
    companion object {
        const val Connecting = 0
        const val Connected = 1
        const val Closing = 2
        const val Closed = 3
        const val Canceled = 4
    }
}

