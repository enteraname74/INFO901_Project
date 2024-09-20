package com.github.enteraname74.info901project.communication.communicatorimpl

import com.github.enteraname74.info901project.communication.EventBusService
import com.github.enteraname74.info901project.domain.model.ClockMessage
import com.github.enteraname74.info901project.domain.model.Communicator
import com.github.enteraname74.info901project.domain.model.message.Message
import com.github.enteraname74.info901project.domain.model.message.UserMessage
import com.github.enteraname74.info901project.domain.model.process.Process
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

    override suspend fun sendMessage(
        withCallback: Boolean,
        block: (safeProcess: Process) -> Message
    ) {
        process?.let { safeProcess ->
            val messageToSend: Message = block(safeProcess)

            if (messageToSend is UserMessage) {
                lamportClock.increment()
            }
            eventBusService.postEvent(
                message = ClockMessage(
                    clock = lamportClock.value,
                    message = block(safeProcess),
                    needCallback = withCallback,
                )
            )
        }
    }

    @Subscribe
    override fun handleMessageReception(clockMessage: ClockMessage) {
        super.handleMessageReception(clockMessage)
    }

    override fun stopCommunication() {
        eventBusService.unRegisterSubscriber(this)
    }
}