package com.github.enteraname74.info901project.domain.model

abstract class Communicator {
    protected val lamportClock: LamportClock = LamportClock()
    protected var senderId: Int? = null
    protected var callback: CommunicatorCallback? = null

    protected abstract fun handleMessageReception(clockMessage: ClockMessage)
    abstract fun send(message: Message)
    abstract fun stopCommunication()

    fun registerSenderId(senderId: Int) {
        this.senderId = senderId
    }
    fun registerCallback(callback: CommunicatorCallback) {
        this.callback = callback
    }
}

interface CommunicatorCallback {
    fun onReceive(message: Message)
}

