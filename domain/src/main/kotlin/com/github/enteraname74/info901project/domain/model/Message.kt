package com.github.enteraname74.info901project.domain.model

import kotlinx.serialization.Serializable

sealed interface Message {
    val senderId: Int

    @Serializable
    data class TokenMessage(
        override val senderId: Int,
        val recipientId: Int,
    ): SystemMessage

    @Serializable
    data class SynchronizationMessage(
        override val senderId: Int,
        val recipientId: Int,
    ): SystemMessage

    @Serializable
    data class BroadcastMessage(
        override val senderId: Int,
        val content: String,
    ): UserMessage

    @Serializable
    data class OneToOneMessage(
        val content: String,
        override val senderId: Int,
        val recipientId: Int,
    ): UserMessage
}

sealed interface SystemMessage : Message
sealed interface UserMessage: Message
