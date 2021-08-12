package com.minivision.moe.entiy

import com.minivision.moe.meta.WisdomSiteEntity

/**
 *
 * @author gf
 * @date 2021/4/22
 */

class Heart(cmd: String = "DevStatus", data: HeartEntity = HeartEntity()) :
    WisdomSiteEntity(cmd, data) {
    override fun requestId(): String {
        return ""
    }

    override fun randomRequestId() {
    }


}


data class HeartEntity(val sn: String = "10001", val ip: String = "192.168.0.251")