package com.github.enteraname74.info901project.domain.model

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
    private val processMessageHandler = ProcessMessageHandler()

    protected abstract fun sendSynchronizeMessage(recipientId: Int)
    abstract suspend fun sendToSync(content: String, recipientId: Int)
    abstract fun sendTo(content: String, recipientId: Int)
    abstract suspend fun receiveFromSync(senderId: Int): Message
    abstract fun stopCommunication()
    abstract fun sendTokenMessage(recipientId: Int)

    protected open fun handleMessageReception(clockMessage: ClockMessage) {
        process?.let { safeProcess ->
            if (clockMessage.message is UserMessage) {
                println("MSG: ${clockMessage.message}")
            }
            val filteredMessage = processMessageHandler.filterMessage(
                message = clockMessage.message,
                process = safeProcess,
            ) ?: return

            when (filteredMessage) {
                is Message.SynchronizationMessage -> systemMailBox.add(filteredMessage)
                is Message.TokenMessage -> {
                    if (state == ProcessState.Request) {
                        println("COMMUNICATOR -- Token -- Process ${process?.name} got token after request")
                        state = ProcessState.CriticalZone
                    } else {
                        sendTokenMessage(recipientId = safeProcess.nextProcessId())
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
        }
    }

    private suspend fun receiveSyncMessage(senderId: Int): Message {
        while(systemMailBox.last()?.senderId != senderId || systemMailBox.last() !is Message.SynchronizationMessage) {
            delay(1_000)
        }
        return systemMailBox.popLast()!!
    }

    abstract suspend fun broadcast(content: String)

    suspend fun synchronize() {
        println("COMMUNICATOR -- synchronize() -- Process ${process?.name} begin sync")
        val next = process?.nextProcessId() ?: return
        val previous = process?.previousProcessId() ?: return

        if (process?.id == 0) {
            sendSynchronizeMessage(recipientId = next)
            receiveSyncMessage(senderId = previous)
            sendSynchronizeMessage(recipientId = next)
        } else {
            receiveSyncMessage(senderId = previous)
            sendSynchronizeMessage(recipientId = next)
            receiveSyncMessage(senderId = previous)
            sendSynchronizeMessage(recipientId = next)
        }

        println("COMMUNICATOR -- synchronize() -- Process ${process?.name} is synchronized")
    }

    fun releaseCriticalZone() {
        process?.let { safeProcess ->
            println("COMMUNICATOR -- Token -- Process ${safeProcess.name} is releasing token, state: $state")
            state = ProcessState.Release
            sendTokenMessage(recipientId = safeProcess.nextProcessId())
        }
    }

    fun requestCriticalZone() {
        println("COMMUNICATOR -- Token -- Process ${process?.name} requestCriticalZone called")
        state = ProcessState.Request
        while(state != ProcessState.CriticalZone) {
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

