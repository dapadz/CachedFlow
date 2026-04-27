package ru.dapadz.cachedflow.cache.keys

import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * The caching key for the [Int] type.
 *
 * @param name key name
 * @see IntegerKey
 */
fun integerCacheKey(name: String): IntegerKey = IntegerKey(name)

/**
 * A class for saving an item of type [Int] to the cache.
 *
 * @param name key name
 * @see Key
 * @see integerCacheKey
 */
class IntegerKey(name: String) : Key<Int>(name) {
    override fun isTypeOf(valueClass: KClass<*>): Boolean = valueClass == Int::class
    override suspend fun getFromStore(store: Store): Flow<Int?> = store.get(StoreKey(name, Int::class))
    override suspend fun saveToStore(item: Int, store: Store) = store.save(StoreKey(name, Int::class), item)
}
