//package com.github.enteraname74.info901project.communication.communicatorimpl
//
//import com.github.enteraname74.info901project.communication.EventBusService
//import com.github.enteraname74.info901project.domain.model.ClockMessage
//import com.github.enteraname74.info901project.domain.model.CommunicationType
//import com.github.enteraname74.info901project.domain.model.Communicator
//import com.github.enteraname74.info901project.domain.model.Message
//import com.google.common.eventbus.Subscribe
//
//class SynchronizationCommunicator : Communicator(
//    type = CommunicationType.Synchronization
//) {
//    private val eventBusService: EventBusService = EventBusService.instance
//
//    init {
//        eventBusService.registerSubscriber(this)
//    }
//
//    override fun isValidMessage(message: Message): Boolean =
//        message is Message.SynchronizationMessage && message.senderId == process?.previousProcessId() &&
//                message.recipientId == process?.id
//
//    @Subscribe
//    override fun handleMessageReception(clockMessage: ClockMessage) {
//        super.handleMessageReception(clockMessage)
//    }
//
//    override fun sendToSync(message: Message) {
//        if (message !is Message.TokenMessage) return
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