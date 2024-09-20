package com.github.enteraname74.info901project.domain.model

import com.github.enteraname74.info901project.domain.model.message.Message

data class ClockMessage(
    val clock: Int,
    val message: Message,
    val needCallback: Boolean = false,
)
