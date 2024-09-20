package com.github.enteraname74.info901project.domain.model.message

import kotlinx.serialization.Serializable

sealed interface Message {
    val senderId: Int

    @Serializable
    data class TokenMessage(
        override val senderId: Int,
        override val recipientId: Int,
    ): SystemMessage

    @Serializable
    data class SynchronizationMessage(
        override val senderId: Int,
        override val recipientId: Int,
    ): SystemMessage

    @Serializable
    data class CallbackMessage(
        override val senderId: Int,
        override val recipientId: Int,
    ): SystemMessage

    @Serializable
    data class BroadcastMessage<T>(
        override val senderId: Int,
        val content: T,
    ): UserMessage

    @Serializable
    data class OneToOneMessage<T>(
        override val senderId: Int,
        val content: T,
        val recipientId: Int,
    ): UserMessage
}

sealed interface SystemMessage : Message {
    val recipientId: Int
}
sealed interface UserMessage: Message
