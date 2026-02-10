package ru.dapadz.cachedflow.logger

interface Logger {
    fun info(tag: String, message: String)
    fun error(tag: String, message: String)
}