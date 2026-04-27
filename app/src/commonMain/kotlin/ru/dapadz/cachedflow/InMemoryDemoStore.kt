package ru.dapadz.cachedflow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.dapadz.cachedflow.store.StoreKey
import kotlin.reflect.KClass

internal class InMemoryDemoStore : DemoInspectableStore {

    private val values = linkedMapOf<StoredEntryKey, Any>()

    override suspend fun clear() {
        values.clear()
    }

    override suspend fun <T : Any> delete(key: StoreKey<T>) {
        values.remove(StoredEntryKey(key.name, key.type))
    }

    override suspend fun <T : Any> get(key: StoreKey<T>): Flow<T?> {
        @Suppress("UNCHECKED_CAST")
        return flowOf(values[StoredEntryKey(key.name, key.type)] as T?)
    }

    override suspend fun <T : Any> save(key: StoreKey<T>, value: T) {
        values[StoredEntryKey(key.name, key.type)] = value
    }

    override suspend fun putRawString(name: String, value: String) {
        values[StoredEntryKey(name, String::class)] = value
    }

    override fun dumpEntries(): Map<String, Any?> {
        val snapshot = linkedMapOf<String, Any?>()
        values.entries
            .sortedBy { it.key.name }
            .forEach { (key, value) ->
                snapshot[key.name] = value
            }
        return snapshot
    }
}

private data class StoredEntryKey(
    val name: String,
    val type: KClass<*>
)
