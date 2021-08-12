package com.minivision.moe.core

import com.minivision.parameter.util.LogUtil

/**
 *
 * @author gf
 * @date 2021/4/22
 */
const val TAG = "MOE"
val LIMIT = 1024

fun LOG_I(message: String) {
    LogUtil.i(TAG, message)
}

fun LOG_D(message: String) {
    LogUtil.d(TAG, message)
}

fun TLOG_I(message: String) {
    LogUtil.i(TAG, "[TCP] $message")
}

fun TLOG_D(message: String) {
    LogUtil.d(TAG, "[TCP] $message")
}

fun ULOG_I(message: String) {
    LogUtil.i(TAG, "[UDP] $message")
}

fun ULOG_D(message: String) {
    LogUtil.d(TAG, "[UDP] $message")
}

fun WLOG_I(message: String) {
    LogUtil.i(TAG, "[WebSocket] $message")
}

fun WLOG_D(message: String) {
    LogUtil.d(TAG, "[WebSocket] $message")
}