package com.github.enteraname74.info901project.communication.communicatorimpl

import com.github.enteraname74.info901project.communication.EventBusService
import com.github.enteraname74.info901project.domain.model.ClockMessage
import com.github.enteraname74.info901project.domain.model.Communicator
import com.github.enteraname74.info901project.domain.model.Message
import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CommunicatorImpl: Communicator() {
    private val eventBusService: EventBusService = EventBusService.instance

    init {
        eventBusService.registerSubscriber(this)
    }

    private suspend fun sendMessage(message: Message) {
        lamportClock.increment()
        eventBusService.postEvent(
            message = ClockMessage(
                clock = lamportClock.value,
                message = message
            )
        )
    }

    override suspend fun sendToSync(content: String, recipientId: Int) {
        process?.let { safeProcess ->
            sendMessage(
                Message.OneToOneMessage(
                    content = content,
                    senderId = safeProcess.id,
                    recipientId = recipientId,
                )
            )
        }
    }

    @Subscribe
    override fun handleMessageReception(clockMessage: ClockMessage) {
        super.handleMessageReception(clockMessage)
    }

    override suspend fun broadcast(content: String) {
        process?.let { safeProcess ->
            println("WILL SEND BROADCAST")
            sendMessage(
                message = Message.BroadcastMessage(
                    senderId = safeProcess.id,
                    content = content,
                )
            )
        }
    }

    override fun sendSynchronizeMessage(recipientId: Int) {
        process?.let { safeProcess ->
            eventBusService.postEvent(
                message = ClockMessage(
                    clock = lamportClock.value,
                    message = Message.SynchronizationMessage(
                        senderId = safeProcess.id,
                        recipientId = safeProcess.nextProcessId(),
                    )
                )
            )
        }
    }

    override fun sendTokenMessage(recipientId: Int) {
        process?.let { safeProcess ->
            eventBusService.postEvent(
                message = ClockMessage(
                    clock = lamportClock.value,
                    message = Message.TokenMessage(
                        senderId = safeProcess.id,
                        recipientId = safeProcess.nextProcessId(),
                    )
                )
            )
        }
    }

    override fun sendTo(content: String, recipientId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            sendToSync(
                content = content,
                recipientId = recipientId,
            )
        }
    }

    override suspend fun receiveFromSync(senderId: Int): Message {
        while(
            mailBox.last()?.senderId != senderId
        ) {
            delay(1_000)
        }

        return mailBox.popLast()!!
    }

    override fun stopCommunication() {
        eventBusService.unRegisterSubscriber(this)
    }
}