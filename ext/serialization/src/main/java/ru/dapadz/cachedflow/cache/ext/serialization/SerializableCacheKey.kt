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

class SerializableCacheKey<T : Any>(
    name: String,
    private val clazz: KClass<T>,
    private val serializer: KSerializer<T>,
    private val module: SerializersModule
) : Key<T>(name) {

    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = module
    }

    override suspend fun getFromStore(store: Store): Flow<T?> {
        return store.get(StoreKey(name, String::class)).map { rawJson ->
            rawJson?.let {
                try {
                    json.decodeFromString(serializer, it)
                } catch (e: Exception) {
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