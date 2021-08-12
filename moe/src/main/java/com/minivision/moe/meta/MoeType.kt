package com.minivision.moe.meta

import androidx.annotation.StringDef

/**
 *
 * @author gf
 * @date 2021/4/22
 */

@StringDef(MoeType.TCP, MoeType.UDP,MoeType.WebSocket)
@Retention(AnnotationRetention.SOURCE)
annotation class MoeType {
    companion object {
        const val TCP = "TCP"
        const val UDP = "UDP"
        const val WebSocket = "WebSocket"
    }
}