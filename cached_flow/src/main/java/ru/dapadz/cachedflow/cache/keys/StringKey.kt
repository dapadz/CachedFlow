package ru.dapadz.cachedflow.cache.keys

import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * The caching key for the [String] type.
 *
 * @param name key name
 * @see StringKey
 */
fun stringCacheKey(name: String): StringKey = StringKey(name)

/**
 * A class for saving an item of type [String] to the cache.
 *
 * @param name key name
 * @see Key
 * @see stringCacheKey
 */
class StringKey(name: String) : Key<String>(name) {
    override fun isTypeOf(valueClass: KClass<*>): Boolean = valueClass == String::class
    override suspend fun saveToStore(item: String, store: Store) = store.save(StoreKey(name, String::class), item)
    override suspend fun getFromStore(store: Store): Flow<String?> = store.get(StoreKey(name, String::class))
}
