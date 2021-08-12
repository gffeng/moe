package com.minivision.moe.post

import com.minivision.moe.core.MoeClient
import java.util.concurrent.Executor

/**
 *  并行发送socket消息的任务，思路取自EventBus
 */
class AsyncPoster(private var mSocketClient: MoeClient, private val mExecutor: Executor) : Runnable,
    IPoster {

    private val queue: PendingPostQueue = PendingPostQueue()

    override fun enqueue(data: ByteArray) {
        val pendingPost = PendingPost.obtainPendingPost(data)
        queue.enqueue(pendingPost)
        mExecutor.execute(this)
    }

    override fun run() {
        if (mSocketClient.isConnected()) {
            val pendingPost =
                queue.poll(200) ?: throw IllegalStateException("No pending post available")
            pendingPost.data?.apply {
                mSocketClient.send(this)
                PendingPost.releasePendingPost(pendingPost)
            }
        }
    }

}