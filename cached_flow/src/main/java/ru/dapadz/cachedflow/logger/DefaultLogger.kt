package ru.dapadz.cachedflow.logger

class DefaultLogger: Logger {
    override fun info(tag: String, message: String) {
        println("ⓘ  $tag:  $message")
    }

    override fun error(tag: String, message: String) {
        println("⛔  $tag:  $message")
    }
}