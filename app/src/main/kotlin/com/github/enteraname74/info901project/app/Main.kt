package com.github.enteraname74.info901project.app

import com.github.enteraname74.info901project.communication.communicatorimpl.CommunicatorImpl
import com.github.enteraname74.info901project.domain.model.process.Process
import kotlinx.coroutines.delay

suspend fun main() {
    val processes: ArrayList<Process> = ArrayList()

    for (i in 0 until Process.MAX_NB_PROCESS) {
        processes.add(
            Process(
                name = "P$i",
                communicator = CommunicatorImpl(),
                id = i,
            )
        )
    }

    processes.firstOrNull()?.sendToken()
    println("Main -- created processes")
    delay(10_000)

    processes.forEach { process ->
        process.stop()
    }

    println("Main -- all processes stopped")
}