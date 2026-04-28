package ru.dapadz.cachedflow

import ru.dapadz.cachedflow.logger.Logger
import ru.dapadz.cachedflow.store.Store

internal data class DemoEnvironment(
    val platformName: String,
    val storeLabel: String,
    val store: DemoInspectableStore,
    val logger: Logger? = null
)

internal interface DemoInspectableStore : Store {
    suspend fun putRawString(name: String, value: String)
    fun dumpEntries(): Map<String, Any?>
}
