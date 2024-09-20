//package com.github.enteraname74.info901project.communication.communicatorimpl
//
//import com.github.enteraname74.info901project.communication.EventBusService
//import com.github.enteraname74.info901project.domain.model.ClockMessage
//import com.github.enteraname74.info901project.domain.model.CommunicationType
//import com.github.enteraname74.info901project.domain.model.Communicator
//import com.github.enteraname74.info901project.domain.model.Message
//import com.google.common.eventbus.Subscribe
//
//internal class BroadcastCommunicator : Communicator(
//    type = CommunicationType.Broadcast,
//) {
//    private val eventBusService: EventBusService = EventBusService.instance
//
//    init {
//        eventBusService.registerSubscriber(this)
//    }
//
//    @Subscribe
//    override fun handleMessageReception(clockMessage: ClockMessage) {
//        super.handleMessageReception(clockMessage)
//    }
//
//    override suspend fun sendToSync(message: Message) {
//        if (message !is Message.BroadcastMessage) return
//
//        lamportClock.increment()
//        eventBusService.postEvent(
//            message = ClockMessage(
//                clock = lamportClock.value,
//                message = message,
//            )
//        )
//    }
//
//    override fun stopCommunication() {
//        eventBusService.unRegisterSubscriber(this)
//    }
//}
//
