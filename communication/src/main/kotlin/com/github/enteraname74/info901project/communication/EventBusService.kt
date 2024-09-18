package com.github.enteraname74.info901project.communication

import com.github.enteraname74.info901project.domain.model.Message
import com.google.common.eventbus.AsyncEventBus
import com.google.common.eventbus.EventBus
import java.util.concurrent.Executors

internal class EventBusService {
    private val eventBus: EventBus = AsyncEventBus(Executors.newCachedThreadPool())

    fun registerSubscriber(subscriber: Any) {
        eventBus.register(subscriber)
    }
    fun unRegisterSubscriber(subscriber: Any) {
        eventBus.unregister(subscriber)
    }

    fun postEvent(message: Any) {
        try {
            eventBus.post(message)
        } catch (e: Exception) {
            println("EventBusService -- postEvent -- Exception: ${e.message}")
        }
    }

    companion object {
        var instance: EventBusService = EventBusService()
    }
}