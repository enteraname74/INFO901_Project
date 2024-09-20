package com.github.enteraname74.info901project.domain.model

class MailBox<T: Message> {
    private val messages: ArrayList<T> = ArrayList()

    fun all(): List<T> = messages
    fun isEmpty(): Boolean = messages.isEmpty()
    fun popLast(): T? =  messages.removeLastOrNull()
    fun last(): T? = messages.lastOrNull()
    fun add(message: T) {
        messages.add(message)
    }
}