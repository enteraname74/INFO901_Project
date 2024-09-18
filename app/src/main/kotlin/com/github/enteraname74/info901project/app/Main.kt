package com.github.enteraname74.info901project.app

import com.github.enteraname74.info901project.communication.communicator.BroadcastCommunicator
import com.github.enteraname74.info901project.domain.model.Process
import kotlinx.coroutines.delay

suspend fun main() {
    val processes: ArrayList<Process> = ArrayList()

    for (i in 0 until Process.MAX_NB_PROCESS) {
        Process(
            name = "P$i",
            communicator = BroadcastCommunicator(),
            id = i,
        )
    }
    println("Main -- created processes")
    delay(10_000)

    processes.forEach { process ->
        process.stop()
    }

    println("Main -- all processes stopped")
}