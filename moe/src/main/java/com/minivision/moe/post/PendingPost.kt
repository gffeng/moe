
package com.minivision.moe.post

class PendingPost private constructor(var data: ByteArray?) {
    var next : PendingPost ?= null

    companion object {
        //复用对象池
        private val pendingPostPool = mutableListOf<PendingPost>()

        /**
         * 首先檢查复用池，如果有则返回复用，否则返回一个新的
         * @return 待发送对象
         */
        fun obtainPendingPost(data: ByteArray): PendingPost {
            synchronized(pendingPostPool) {
                val size = pendingPostPool.size
                if (size > 0) {
                    val pendingPost = pendingPostPool.removeAt(size - 1)
                    pendingPost.data = data
                    pendingPost.next = null
                    return pendingPost
                }
            }
            return PendingPost(data)
        }

        /**
         * 回收一个待发送对象，加入复用池
         */
        fun releasePendingPost(pendingPost: PendingPost) {
            pendingPost.data = null
            pendingPost.next = null
            synchronized(pendingPostPool) {
                if (pendingPostPool.size < 10000) {
                    pendingPostPool.add(pendingPost)
                }
            }
        }
    }

}