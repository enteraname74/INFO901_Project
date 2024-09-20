@file:JvmName("Main")
package com.github.enteraname74.info901project.app

import com.github.enteraname74.info901project.communication.communicatorimpl.CommunicatorImpl
import com.github.enteraname74.info901project.domain.model.process.Process
import kotlinx.coroutines.delay

const val MAX_NB_PROCESS = 3

suspend fun main() {
    val processes: ArrayList<Process> = ArrayList()

    for (i in 0 until MAX_NB_PROCESS) {
        processes.add(
            Process(communicator = CommunicatorImpl())
        )
    }

    println("Main -- will init process to give them ids")
    processes.forEach { process ->
        process.run()
    }

    println("Main -- created processes")
    delay(20_000)

    processes.forEach { process ->
        process.stop()
    }

    println("Main -- all processes stopped")
}