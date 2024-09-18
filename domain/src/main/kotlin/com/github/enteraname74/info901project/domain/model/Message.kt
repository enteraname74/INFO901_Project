package com.github.enteraname74.info901project.domain.model

import kotlinx.serialization.Serializable

sealed interface Message {
    val senderId: Int

    @Serializable
    data class BroadcastMessage(
        val content: String,
        override val senderId: Int
    ): Message

    @Serializable
    data class TokenMessage(
        override val senderId: Int
    ): Message

    @Serializable
    data class OneToOneMessage(
        override val senderId: Int,
        val recipientId: Int,
    ): Message
}
