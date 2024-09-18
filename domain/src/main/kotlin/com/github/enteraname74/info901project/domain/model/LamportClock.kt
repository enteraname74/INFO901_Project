package com.github.enteraname74.info901project.domain.model

class LamportClock {
    var value: Int = 0
        private set

    fun increment() {
        value += 1
    }

    fun setMax(other: Int) {
        value = maxOf(value, other) + 1
    }
}
