package ru.dapadz.cachedflow.cache.keys

import kotlinx.coroutines.flow.Flow
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlin.reflect.KClass

/**
 * The caching key for the [Float] type.
 *
 * @param name key name
 * @see FloatKey
 */
fun floatCacheKey(name: String): FloatKey = FloatKey(name)

/**
 * A class for saving an item of type [Float] to the cache.
 *
 * @param name key name
 * @see Key
 * @see floatCacheKey
 */
class FloatKey(name: String) : Key<Float>(name) {
    override fun isTypeOf(valueClass: KClass<*>): Boolean = valueClass == Float::class
    override suspend fun getFromStore(store: Store): Flow<Float?> = store.get(StoreKey(name, Float::class))
    override suspend fun saveToStore(item: Float, store: Store) = store.save(StoreKey(name, Float::class), item)
}
