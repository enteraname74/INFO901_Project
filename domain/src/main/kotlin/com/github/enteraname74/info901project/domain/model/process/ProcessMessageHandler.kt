package com.github.enteraname74.info901project.domain.model.process

import com.github.enteraname74.info901project.domain.model.message.Message

class ProcessMessageHandler {
    private fun handleCallbackMessage(
        message: Message.CallbackMessage,
        processId: Int,
    ): Message? =
        message.takeUnless {
            it.senderId == processId || it.recipientId != processId
        }

    private fun handleBroadcastMessage(
        message: Message.BroadcastMessage<*>,
        processId: Int,
    ): Message? =
        message.takeUnless { it.senderId == processId }

    private fun handleOneToOneMessage(
        message: Message.OneToOneMessage<*>,
        processId: Int,
    ): Message? =
        message.takeUnless {
            it.senderId == processId || it.recipientId != processId
        }

    private fun handleTokenMessage(
        message: Message.TokenMessage,
        processId: Int,
        previousId: Int,
    ): Message? =
        message.takeUnless {
            it.senderId == processId ||
                    it.senderId != previousId ||
                    it.recipientId != processId
        }

    private fun handleSynchronizationMessage(
        message: Message.SynchronizationMessage,
        processId: Int,
        previousId: Int,
    ): Message? =
        message.takeUnless {
            it.senderId == processId ||
                    it.senderId != previousId ||
                    it.recipientId != processId
        }

    fun filterMessage(
        message: Message,
        process: Process,
    ): Message? =
        when(message) {
            is Message.CallbackMessage -> handleCallbackMessage(
                message = message,
                processId = process.id,
            )
            is Message.BroadcastMessage<*> -> handleBroadcastMessage(
                message = message,
                processId = process.id
            )
            is Message.OneToOneMessage<*> -> handleOneToOneMessage(
                message = message,
                processId = process.id,
            )
            // Sync and token message are skip for the process. They are only useful for the communicator
            is Message.TokenMessage -> handleTokenMessage(
                message = message,
                processId = process.id,
                previousId = process.previousProcessId(),
            )
            is Message.SynchronizationMessage -> handleSynchronizationMessage(
                message = message,
                processId = process.id,
                previousId = process.previousProcessId(),
            )
        }
}