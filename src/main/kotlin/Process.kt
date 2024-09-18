import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Process(
    private val name: String,
    private val id: Int,
) {
    private val bus: EventBusService = EventBusService.instance
    private var alive: Boolean = true
    private var dead: Boolean = false

    init {
        bus.registerSubscriber(this)
        CoroutineScope(Dispatchers.IO).launch {
            run()
        }
    }

    @Subscribe
    fun onMessageOnBus(message: Message) {
        println("Process $name received: $message")
    }

    suspend fun run() {
        var lap = 0
        println("Process $name launched")
        while (alive) {
            try {
                delay(500)
                if (name == "P1") {
                    val message = Message("Hello World!")
                    println("Process $name send $message")
                    bus.postEvent(message)
                }
            } catch (e: Exception) {
                println("Process -- Exception in process $name: ${e.message}")
            }

            lap += 1
        }

        bus.unRegisterSubscriber(this)
        dead = true
    }

    suspend fun waitStopped() {
        while (!dead) {
            delay(500)
        }
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