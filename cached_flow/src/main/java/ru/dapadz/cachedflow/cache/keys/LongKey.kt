package ru.dapadz.cachedflow.cache.keys

import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * The caching key for the [Long] type.
 *
 * @param name key name
 * @see LongKey
 */
fun longCacheKey(name: String): LongKey = LongKey(name)

/**
 * A class for saving an item of type [Long] to the cache.
 *
 * @param name key name
 * @see Key
 * @see longCacheKey
 */
class LongKey(name: String) : Key<Long>(name) {
    override fun isTypeOf(valueClass: KClass<*>): Boolean = valueClass == Long::class
    override suspend fun getFromStore(store: Store): Flow<Long?> = store.get(StoreKey(name, Long::class))
    override suspend fun saveToStore(item: Long, store: Store) = store.save(StoreKey(name, Long::class), item)
}
