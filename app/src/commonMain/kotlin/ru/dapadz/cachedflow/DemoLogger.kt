package ru.dapadz.cachedflow

import ru.dapadz.cachedflow.logger.DefaultLogger
import ru.dapadz.cachedflow.logger.Logger

internal class DemoLogger(
    private val logStore: DemoLogStore,
    delegate: Logger?
) : Logger {

    private val delegateLogger = delegate ?: DefaultLogger()

    override fun info(tag: String, message: String) {
        delegateLogger.info(tag, message)
        logStore.add("CF/$tag", message)
    }

    override fun error(tag: String, message: String) {
        delegateLogger.error(tag, message)
        logStore.add("ERR/$tag", message)
    }
}
