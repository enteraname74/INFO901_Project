package com.github.enteraname74.info901project.domain.model

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LamportClock {
    var value: Int = 0
        private set

    private val mutex: Mutex = Mutex()

    suspend fun increment() {
        mutex.withLock {
            value += 1
        }
    }

    suspend fun setMax(other: Int) {
        mutex.withLock {
            value = maxOf(value, other) + 1
        }
    }
}
