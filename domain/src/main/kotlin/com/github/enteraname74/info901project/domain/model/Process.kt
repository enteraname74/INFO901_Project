package com.github.enteraname74.info901project.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Process(
    private val name: String,
    private val id: Int,
    private val communicator: Communicator,
): CommunicatorCallback {
    private var alive: Boolean = true
    private var dead: Boolean = false

    init {
        with(communicator) {
            registerSenderId(senderId = id)
            registerCallback(this@Process)
        }

        CoroutineScope(Dispatchers.IO).launch {
            run()
        }
    }

    override fun onReceive(message: Message) {
        println("Process $name receives: $message")
    }

    suspend fun run() {
        var lap = 0
        println("Process $name launched")
        while (alive) {
            try {
                delay(500)
                if (id == 0) {
                    val message = "Hello World!"
                    println("Process $name send $message")
                    communicator.send(
                        Message.BroadcastMessage(
                            content = message,
                            senderId = id,
                        )
                    )
                }
            } catch (e: Exception) {
                println("Process -- Exception in process $name: ${e.message}")
            }

            lap += 1
        }

        communicator.stopCommunication()
        dead = true
    }

    fun stop() {
        println("Process $name, stop() called")
        alive = false
    }

    companion object {
        const val MAX_NB_PROCESS = 3
        const val NB_PROCESS = MAX_NB_PROCESS - 1
    }
}