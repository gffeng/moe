package com.minivision.moe.post

import com.minivision.moe.core.MoeClient
import java.util.concurrent.Executor

class SyncPoster(private val mSocketClient: MoeClient, private val mExecutor: Executor) : Runnable,
    IPoster {

    private val queue: PendingPostQueue = PendingPostQueue()

    @Volatile
    private var executorRunning: Boolean = false

    override fun enqueue(data: ByteArray) {
        val pendingPost = PendingPost.obtainPendingPost(data)
        synchronized(this) {
            queue.enqueue(pendingPost)
            if (!executorRunning) {
                executorRunning = true
                mExecutor.execute(this)
            }
        }
    }

    override fun run() {
        try {
            try {
                while (true) {
                    var pendingPost = queue.poll(1000)
                    if (pendingPost == null) {
                        synchronized(this) {
                            pendingPost = queue.poll()
                            if (pendingPost == null) {
                                executorRunning = false
                                return
                            }
                        }
                    }
                    pendingPost?.data?.apply {
                        mSocketClient.send(this)
                        PendingPost.releasePendingPost(pendingPost!!)
                    }
                }
            } catch (e: InterruptedException) {
                e.toString()
            }

        } finally {
            executorRunning = false
        }
    }
}