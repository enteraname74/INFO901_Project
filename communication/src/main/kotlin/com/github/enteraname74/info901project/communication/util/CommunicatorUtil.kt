//package com.github.enteraname74.info901project.communication.util
//
//import com.github.enteraname74.info901project.communication.communicatorimpl.BroadcastCommunicator
//import com.github.enteraname74.info901project.communication.communicatorimpl.OneToOneCommunicator
//import com.github.enteraname74.info901project.communication.communicatorimpl.SynchronizationCommunicator
//import com.github.enteraname74.info901project.communication.communicatorimpl.TokenCommunicator
//import com.github.enteraname74.info901project.domain.model.CommunicationType
//import com.github.enteraname74.info901project.domain.model.Communicator
//
//object CommunicatorUtil {
//    fun buildFromType(communicationType: CommunicationType): Communicator =
//        when (communicationType) {
//            CommunicationType.Broadcast -> BroadcastCommunicator()
//            CommunicationType.OneToOne -> OneToOneCommunicator()
//            CommunicationType.Token -> TokenCommunicator()
//            CommunicationType.Synchronization -> SynchronizationCommunicator()
//        }
//}