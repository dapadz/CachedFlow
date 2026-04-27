package ru.dapadz.cachedflow

import ru.dapadz.cachedflow.cache.android.AndroidLogger
import ru.dapadz.cachedflow.logger.Logger

class DemoLogger : Logger {

    private val delegate = AndroidLogger()

    override fun info(tag: String, message: String) {
        delegate.info(tag, message)
        DemoLogStore.add("CF/$tag", message)
    }

    override fun error(tag: String, message: String) {
        delegate.error(tag, message)
        DemoLogStore.add("ERR/$tag", message)
    }
}
