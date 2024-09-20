package com.github.enteraname74.info901project.domain.model.process

import com.github.enteraname74.info901project.domain.model.Communicator
import com.github.enteraname74.info901project.domain.model.CommunicatorCallback
import com.github.enteraname74.info901project.domain.model.message.Message
import kotlinx.coroutines.*

class Process(
    val name: String,
    val id: Int,
    private val communicator: Communicator,
) : CommunicatorCallback {
    private var alive: Boolean = true
    private var dead: Boolean = false
    private var job: Job? = null

    init {
        with(communicator) {
            registerProcess(process = this@Process)
            registerCallback(this@Process)
        }

        job = CoroutineScope(Dispatchers.IO).launch {
            run()
        }
    }

    override fun onReceive(message: Message) {
        println("Process $name received message: $message")
    }

    suspend fun sendToken() {
        communicator.sendTokenMessage()
    }

    private suspend fun handleCriticalZone() {
        communicator.requestCriticalZone()
        if (communicator.mailBox.isEmpty()) {
            println("Process $name: J'ai gagné !!!")
            communicator.broadcast("J'ai gagné !!!")
        } else {
            val messageFromMailBox: Message? = communicator.mailBox.popLast()
            messageFromMailBox?.let { println("Process $name, ${it.senderId} a eu le jeton en premier") }
        }
        communicator.releaseCriticalZone()
    }

    private suspend fun run() {
        var lap = 0
        println("Process $name launched")
        while (alive) {
            try {
                delay(1_000)
                if (id == 0) {
                    communicator.broadcastSync(
                        content = "Message à tous les participants"
                    )

                    communicator.sendTo(
                        content = "J'appelle 2 et je te recontacte après",
                        recipientId = 1
                    )
                    communicator.sendToSync(
                        content = "J'ai laissé un message à 2, " +
                                "je le rappellerai après, " +
                                "on se synchronise tous et on attaque la partie ?",
                        recipientId = 2,
                    )
                    val messageFrom2: Message = communicator.receiveFromSync(senderId = 2)
                    println("Process $name received message from process 2: $messageFrom2")
                    communicator.sendToSync(
                        content = "2 est OK pour jouer, on se synchronise et c'est parti!",
                        recipientId = 1,
                    )
                    communicator.synchronize()
                    handleCriticalZone()

                } else if (id == 1) {
                    val initialMessageFrom0 = communicator.receiveFromSync(senderId = 0)
                    println("Process $name received message from process 0: $initialMessageFrom0")

                    if (!communicator.mailBox.isEmpty()) {
                        val messageFromMailBox: Message? = communicator.mailBox.popLast()
                        println("Process $name got message from mail box: $messageFromMailBox")
                        val messageFrom0: Message = communicator.receiveFromSync(senderId = 0)
                        println("Process $name received message from 0: $messageFrom0")

                        communicator.synchronize()
                        handleCriticalZone()
                    }
                } else if (id == 2) {
                    val initMessageFrom0 = communicator.receiveFromSync(senderId = 0)
                    println("Process $name received message from process 0: $initMessageFrom0")
                    val messageFrom0: Message = communicator.receiveFromSync(
                        senderId = 0
                    )
                    println("Process $name got message from 0: $messageFrom0")
                    communicator.sendToSync(
                        content = "OK",
                        recipientId = 0,
                    )

                    communicator.synchronize()
                    handleCriticalZone()
                }
            } catch (e: Exception) {
                println("Process $name-- Exception in process $name: ${e.message}")
            }

            lap += 1
        }

        println("Process $name, end of run()")
        dead = true
    }

    fun stop() {
        println("Process $name, stop() called")
        alive = false
        communicator.stopCommunication()
        job?.cancel()
        job = null
    }

    fun nextProcessId(): Int =
        (id + 1) % MAX_NB_PROCESS

    fun previousProcessId(): Int =
        (id - 1 + MAX_NB_PROCESS) % MAX_NB_PROCESS

    companion object {
        const val MAX_NB_PROCESS = 3
    }
}