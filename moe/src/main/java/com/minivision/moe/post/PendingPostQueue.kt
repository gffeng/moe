package com.minivision.moe.post

class PendingPostQueue {
    private var head: PendingPost? = null //待发送对象队列头节点
    private var tail: PendingPost? = null //待发送对象队列尾节点
    private val lock = Object()

    /**
     *  入队
     */
    fun enqueue(pendingPost: PendingPost) = synchronized(lock) {
        if (tail != null) {
            tail!!.next = pendingPost
            tail = pendingPost
        } else if (head == null) {
            tail = pendingPost
            head = tail
        } else {
            throw IllegalStateException("Head present, but no tail")
        }
        lock.notifyAll()
    }

    /**
     * 取队列头节点的待发送对象
     */
    fun poll(): PendingPost? = synchronized(lock){
        val pendingPost = head
        head?.let {
            head = it.next
            if (head == null) {
                tail = null
            }
        }
        return pendingPost
    }

    /**
     * 取队列头节点的待发送对象
     */
    @Throws(InterruptedException::class)
    fun poll(maxMillisToWait: Int): PendingPost? = synchronized(lock){
        if (head == null) {
            lock.wait(maxMillisToWait.toLong())
        }
        return poll()
    }

}