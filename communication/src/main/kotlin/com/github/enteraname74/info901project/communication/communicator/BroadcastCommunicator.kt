package com.github.enteraname74.info901project.communication.communicator

import com.github.enteraname74.info901project.communication.EventBusService
import com.github.enteraname74.info901project.domain.model.ClockMessage
import com.github.enteraname74.info901project.domain.model.Communicator
import com.github.enteraname74.info901project.domain.model.Message
import com.google.common.eventbus.Subscribe

class BroadcastCommunicator : Communicator() {
    private val eventBusService: EventBusService = EventBusService.instance

    init {
        eventBusService.registerSubscriber(this)
    }

    @Subscribe
    override fun handleMessageReception(clockMessage: ClockMessage) {
        lamportClock.setMax(clockMessage.clock)

        if (clockMessage.message !is Message.BroadcastMessage) return

        if (clockMessage.message.senderId != senderId) {
            lamportClock.setMax(other = clockMessage.clock)
            callback?.onReceive(
                message = clockMessage.message,
            )
        }
    }


    override fun send(message: Message) {
        lamportClock.increment()
        eventBusService.postEvent(
            message = ClockMessage(
                clock = lamportClock.value,
                message = message,
            )
        )
    }

    override fun stopCommunication() {
        eventBusService.unRegisterSubscriber(this)
    }
}

