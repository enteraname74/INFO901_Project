import kotlinx.coroutines.delay

suspend fun main() {
    val processes: ArrayList<Process> = ArrayList()

    for (i in 0 until Process.MAX_NB_PROCESS) {
        processes.add(
            Process(
                name = "P$i",
                id = i
            )
        )
    }
    println("Main -- created processes")
    delay(10_000)

    processes.forEach { process ->
        process.stop()
    }

    println("Main -- all processes stopped")
}