package ru.dapadz.cachedflow.testsupport

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.dapadz.cachedflow.logger.Logger
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlin.reflect.KClass

internal object SilentLogger : Logger {
    override fun info(tag: String, message: String) = Unit
    override fun error(tag: String, message: String) = Unit
}

internal class InMemoryStore : Store {

    private val values = mutableMapOf<StoredEntryKey, Any>()
    val saveCalls = mutableListOf<SaveCall>()
    var clearCalls: Int = 0
        private set

    override suspend fun <T : Any> get(key: StoreKey<T>): Flow<T?> {
        @Suppress("UNCHECKED_CAST")
        val value = values[StoredEntryKey(key.name, key.type)] as T?
        return flowOf(value)
    }

    override suspend fun <T : Any> save(key: StoreKey<T>, value: T) {
        values[StoredEntryKey(key.name, key.type)] = value
        saveCalls += SaveCall(key.name, key.type, value)
    }

    override suspend fun <T : Any> delete(key: StoreKey<T>) {
        values.remove(StoredEntryKey(key.name, key.type))
    }

    override suspend fun clear() {
        values.clear()
        clearCalls++
    }

    fun <T : Any> putRaw(name: String, type: KClass<T>, value: T) {
        values[StoredEntryKey(name, type)] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> readRaw(name: String, type: KClass<T>): T? {
        return values[StoredEntryKey(name, type)] as T?
    }
}

internal data class SaveCall(
    val name: String,
    val type: KClass<*>,
    val value: Any
)

private data class StoredEntryKey(
    val name: String,
    val type: KClass<*>
)
