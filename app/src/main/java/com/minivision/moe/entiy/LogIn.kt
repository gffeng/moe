package com.minivision.moe.entiy

import com.minivision.moe.util.MoeUtils

/**
 *
 * @author gf
 * @date 2021/5/17
 */
class LogIn {

    val cmd = 823

    val code = "HC2021-ZHGD-ZRYZN-2021-GSLMLGLD"

    val sn = "gldtest1"
    val xo = MoeUtils.getXor(code + sn)


    override fun toString(): String {
        return MoeUtils.string2Hex(code, 32) + MoeUtils.string2Hex(sn, 32) + xo + " "
    }
}


