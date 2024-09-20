package com.github.enteraname74.info901project.domain.model.process

import com.github.enteraname74.info901project.domain.model.Communicator
import com.github.enteraname74.info901project.domain.model.message.Message
import kotlinx.coroutines.*

class Process(
    private val communicator: Communicator,
) {
    private var alive: Boolean = true
    private var dead: Boolean = false
    private var job: Job? = null
    var id: Int = 0
    val name: String
        get() = "P$id"
    private var totalNumberOfProcess: Int = 1

    init {
        with(communicator) {
            registerProcess(process = this@Process)
        }
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

    private suspend fun init() {
        communicator.initIds()
        while(true) {
            val randomNumber: Int = (0..MAX_RANDOM).random()
            // We temporally set the id of the process to be the one we fetch randomly (will simplify the communicator process)
            println("Process $name got a random number: $randomNumber")
            id = randomNumber

            // We send the id to the communicator
            communicator.broadcast(content = randomNumber)

            // We retrieve the response from the communicator
            val newId: Message.IdMessage? = communicator.receiveFromSync(senderId = id) as? Message.IdMessage
            println("Process $name got an id message: $newId")

            // The communicator send us a good id to use
            if (newId?.validId != null) {
                id = newId.validId
                totalNumberOfProcess = newId.numberOfProcesses
                return
            }
            // If we have not a valid id, we will send a new one (just loop again)
        }
    }

    fun run() {
        job = CoroutineScope(Dispatchers.IO).launch {
            // We first init the process to retrieve a unique id
            init()

            // Initialization of the token:
            if (id == 0) {
                communicator.sendTokenMessage()
            }

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
                        println("Process $name will start synchro")
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
    }

    fun stop() {
        println("Process $name, stop() called")
        alive = false
        communicator.stopCommunication()
        job?.cancel()
        job = null
    }

    fun nextProcessId(): Int =
        (id + 1) % totalNumberOfProcess

    fun previousProcessId(): Int =
        (id - 1 + totalNumberOfProcess) % totalNumberOfProcess

    companion object {
        const val MAX_RANDOM = 100_000
    }
}