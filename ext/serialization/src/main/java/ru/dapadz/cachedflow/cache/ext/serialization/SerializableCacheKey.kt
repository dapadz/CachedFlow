package ru.dapadz.cachedflow.cache.ext.serialization

import ru.dapadz.cachedflow.cache.keys.Key
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

/**
 * [Key] implementation that persists arbitrary serializable objects as JSON.
 *
 * Values are encoded into a `String` before being saved to the underlying
 * [Store], then decoded back into [T] when read. This allows stores that only
 * understand primitive values, such as `SharedPreferences`, to cache richer
 * domain models.
 *
 * When cached JSON can no longer be decoded, the key treats it as a cache miss
 * and emits `null` instead of failing the flow. This keeps consumers resilient
 * to schema changes or manually corrupted cache data.
 *
 * @param name Unique cache entry name.
 * @param clazz Runtime class of the cached value.
 * @param serializer Serializer used to encode and decode [T].
 * @param module Additional serializer registrations used by [serializer].
 *
 * @see serializableKey
 * @see serializableListKey
 */
class SerializableCacheKey<T : Any>(
    name: String,
    private val clazz: KClass<T>,
    private val serializer: KSerializer<T>,
    private val module: SerializersModule
) : Key<T>(name) {

    private val json = Json {
        // Allow older cache payloads to survive additive model changes.
        ignoreUnknownKeys = true
        serializersModule = module
    }

    override suspend fun getFromStore(store: Store): Flow<T?> {
        return store.get(StoreKey(name, String::class)).map { rawJson ->
            rawJson?.let {
                try {
                    json.decodeFromString(serializer, it)
                } catch (e: Exception) {
                    // Corrupted or outdated JSON is treated as a cache miss.
                    null
                }
            }
        }
    }

    override suspend fun saveToStore(item: T, store: Store) {
        val jsonString = json.encodeToString(serializer, item)
        store.save(StoreKey(name, String::class), jsonString)
    }

    override fun isTypeOf(valueClass: KClass<*>): Boolean {
        return valueClass == clazz
    }
}
