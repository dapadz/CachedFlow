package ru.dapadz.cachedflow.cache.keys

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlin.reflect.KClass

/**
 * The caching key for the [Char] type.
 *
 * @param name key name
 * @see CharKey
 */
fun charCacheKey(name: String): CharKey = CharKey(name)

/**
 * A class for saving an item of type [Char] to the cache.
 *
 * @param name key name
 * @see Key
 * @see charCacheKey
 */
class CharKey(name: String) : Key<Char>(name) {
    override fun isTypeOf(valueClass: KClass<*>): Boolean = valueClass == Char::class

    override suspend fun getFromStore(store: Store): Flow<Char?> {
        return store.get(StoreKey(name, String::class)).map { value ->
            value?.singleOrNull()
        }
    }

    override suspend fun saveToStore(item: Char, store: Store) {
        store.save(StoreKey(name, String::class), item.toString())
    }
}
