package com.github.enteraname74.info901project.domain.model

import com.github.enteraname74.info901project.domain.model.message.Message
import com.github.enteraname74.info901project.domain.model.message.SystemMessage
import com.github.enteraname74.info901project.domain.model.message.UserMessage
import com.github.enteraname74.info901project.domain.model.process.Process
import com.github.enteraname74.info901project.domain.model.process.ProcessMessageHandler
import com.github.enteraname74.info901project.domain.model.process.ProcessState
import kotlinx.coroutines.*

abstract class Communicator {
    protected val lamportClock: LamportClock = LamportClock()
    protected var process: Process? = null
    private var callback: CommunicatorCallback? = null
    private var state: ProcessState = ProcessState.Idle
    val mailBox: MailBox<UserMessage> = MailBox()
    private val systemMailBox: MailBox<SystemMessage> = MailBox()
    private val callbackMailBox: MailBox<Message.CallbackMessage> = MailBox()
    private val processMessageHandler = ProcessMessageHandler()

    protected abstract suspend fun sendMessage(
        withCallback: Boolean = false,
        block: (safeProcess: Process) -> Message
    )
    abstract fun stopCommunication()

    private suspend fun sendSynchronizeMessage() {
        sendMessage { safeProcess ->
            Message.SynchronizationMessage(
                senderId = safeProcess.id,
                recipientId = safeProcess.nextProcessId(),
            )
        }
    }

    private suspend fun waitCallback(senderId: Int): Message? {
        while (callbackMailBox.last()?.senderId != senderId || callbackMailBox.last()?.recipientId != process?.id) {
            delay(1_000)
        }
        return callbackMailBox.popLast()
    }

    /**
     * Wait for a number of callbacks to appear in the callback mailbox.
     */
    private suspend fun waitCallbacks(totalToFetch: Int) {
        while (callbackMailBox.all().size < totalToFetch) {
            delay(1_000)
        }
        callbackMailBox.clear()
    }

    private suspend fun receiveSyncMessage(senderId: Int): Message {
        while (systemMailBox.last()?.senderId != senderId || systemMailBox.last() !is Message.SynchronizationMessage) {
            delay(1_000)
        }
        return systemMailBox.popLast()!!
    }

    protected open fun handleMessageReception(clockMessage: ClockMessage) {
        process?.let { safeProcess ->
            val filteredMessage = processMessageHandler.filterMessage(
                message = clockMessage.message,
                process = safeProcess,
            ) ?: return


            when (filteredMessage) {
                is Message.SynchronizationMessage -> systemMailBox.add(filteredMessage)
                is Message.CallbackMessage -> {
                    println("COMMUNICATOR -- handleMessageReception() -- Process ${process?.name} got callback message")
                    callbackMailBox.add(filteredMessage)
                }
                is Message.TokenMessage -> {
                    if (state == ProcessState.Request) {
                        println("COMMUNICATOR -- Token -- Process ${process?.name} got token after request")
                        state = ProcessState.CriticalZone
                    } else {
                        runBlocking {
                            sendTokenMessage()
                        }
                    }
                }
                is UserMessage -> {
                    // System messages don't increment lamport clock
                    runBlocking {
                        lamportClock.setMax(other = clockMessage.clock)
                    }
                    mailBox.add(message = filteredMessage)
                    callback?.onReceive(message = filteredMessage)
                }
            }

            if (clockMessage.needCallback) {
                println("COMMUNICATION -- handleMessageReception() -- Process ${process?.name} should send a callback " +
                        "to ${clockMessage.message.senderId}")
                runBlocking {
                    sendMessage { safeProcess ->
                        Message.CallbackMessage(
                            senderId = safeProcess.id,
                            recipientId = clockMessage.message.senderId,
                        )
                    }
                }
            }
        }
    }

    suspend fun <T> sendToSync(content: T, recipientId: Int) {
        println("COMMUNICATOR -- sendToSync() -- Process ${process?.name} will send message to $recipientId")
        sendMessage(withCallback = true) { safeProcess ->
            Message.OneToOneMessage(
                content = content,
                senderId = safeProcess.id,
                recipientId = recipientId,
            )
        }
        // We wait for the callback from the recipient id:
        val recipientAnswer = waitCallback(senderId = recipientId)
        println("COMMUNICATOR -- sendToSync() -- Process ${process?.name} got callback: $recipientAnswer")
    }

    suspend fun <T>sendTo(content: T, recipientId: Int) {
        sendMessage { safeProcess ->
            Message.OneToOneMessage(
                senderId = safeProcess.id,
                content = content,
                recipientId = recipientId,
            )
        }
    }

    suspend fun sendTokenMessage() {
        sendMessage { safeProcess ->
            Message.TokenMessage(
                senderId = safeProcess.id,
                recipientId = safeProcess.nextProcessId(),
            )
        }
    }

    suspend fun <T> broadcast(content: T) {
        sendMessage { safeProcess ->
            Message.BroadcastMessage(
                senderId = safeProcess.id,
                content = content,
            )
        }
    }

    suspend fun <T> broadcastSync(content: T) {
        sendMessage(withCallback = true) { safeProcess ->
            Message.BroadcastMessage(
                senderId = safeProcess.id,
                content = content,
            )
        }
        waitCallbacks(totalToFetch = Process.MAX_NB_PROCESS - 1)
    }

    suspend fun receiveFromSync(senderId: Int): Message {
        while(mailBox.last()?.senderId != senderId) {
            delay(1_000)
        }

        return mailBox.popLast()!!
    }

    suspend fun synchronize() {
        println("COMMUNICATOR -- synchronize() -- Process ${process?.name} begin sync")
        val previous = process?.previousProcessId() ?: return

        if (process?.id == 0) {
            sendSynchronizeMessage()
            receiveSyncMessage(senderId = previous)
            sendSynchronizeMessage()
        } else {
            receiveSyncMessage(senderId = previous)
            sendSynchronizeMessage()
            receiveSyncMessage(senderId = previous)
            sendSynchronizeMessage()
        }

        println("COMMUNICATOR -- synchronize() -- Process ${process?.name} is synchronized")
    }

    suspend fun releaseCriticalZone() {
        process?.let { safeProcess ->
            println("COMMUNICATOR -- Token -- Process ${safeProcess.name} is releasing token, state: $state")
            state = ProcessState.Release
            sendTokenMessage()
        }
    }

    fun requestCriticalZone() {
        println("COMMUNICATOR -- Token -- Process ${process?.name} requestCriticalZone called")
        state = ProcessState.Request
        while (state != ProcessState.CriticalZone) {
            runBlocking {
                delay(1000)
            }
            if (state != ProcessState.Request) {
                println("STATE CHANGED: $state")
                return
            }
        }
        println("COMMUNICATOR -- Token -- Process ${process?.name} is quitting request, state: $state")
    }

    fun registerProcess(process: Process) {
        this.process = process
    }

    fun registerCallback(callback: CommunicatorCallback) {
        this.callback = callback
    }
}

interface CommunicatorCallback {
    fun onReceive(message: Message)
}

