package ru.dapadz.cachedflow.cache.keys

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlin.reflect.KClass

/**
 * The caching key for the [Double] type.
 *
 * @param name key name
 * @see DoubleKey
 */
fun doubleCacheKey(name: String): DoubleKey = DoubleKey(name)

/**
 * A class for saving an item of type [Double] to the cache.
 *
 * @param name key name
 * @see Key
 * @see doubleCacheKey
 */
class DoubleKey(name: String) : Key<Double>(name) {
    override fun isTypeOf(valueClass: KClass<*>): Boolean = valueClass == Double::class

    override suspend fun getFromStore(store: Store): Flow<Double?> {
        return store.get(StoreKey(name, Long::class)).map { value ->
            value?.let(Double::fromBits)
        }
    }

    override suspend fun saveToStore(item: Double, store: Store) {
        store.save(StoreKey(name, Long::class), item.toBits())
    }
}
