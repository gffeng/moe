package com.minivision.moe.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import java.io.Closeable
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


/**
 * 读取socket线程池
 */
val mExecutor = ThreadPoolExecutor(
    2, Integer.MAX_VALUE,
    60L, TimeUnit.SECONDS, SynchronousQueue()
) { r ->
    Thread(r, "IoExecutor")
}

/**
 * 协程
 */
val scope: CoroutineScope
    get() {
        return CloseableCoroutineScope(mExecutor.asCoroutineDispatcher())
    }

internal class CloseableCoroutineScope(context: CoroutineContext) : Closeable, CoroutineScope {
    override val coroutineContext: CoroutineContext = context

    override fun close() {
        coroutineContext.cancel()
    }
}