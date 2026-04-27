package ru.dapadz.cachedflow.cache.keys

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlin.reflect.KClass

/**
 * The caching key for the [Short] type.
 *
 * @param name key name
 * @see ShortKey
 */
fun shortCacheKey(name: String): ShortKey = ShortKey(name)

/**
 * A class for saving an item of type [Short] to the cache.
 *
 * @param name key name
 * @see Key
 * @see shortCacheKey
 */
class ShortKey(name: String) : Key<Short>(name) {
    override fun isTypeOf(valueClass: KClass<*>): Boolean = valueClass == Short::class

    override suspend fun getFromStore(store: Store): Flow<Short?> {
        return store.get(StoreKey(name, Int::class)).map { value ->
            value
                ?.takeIf { it in Short.MIN_VALUE.toInt()..Short.MAX_VALUE.toInt() }
                ?.toShort()
        }
    }

    override suspend fun saveToStore(item: Short, store: Store) {
        store.save(StoreKey(name, Int::class), item.toInt())
    }
}
