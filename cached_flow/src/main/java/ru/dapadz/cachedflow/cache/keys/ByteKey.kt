package ru.dapadz.cachedflow.cache.keys

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlin.reflect.KClass

/**
 * The caching key for the [Byte] type.
 *
 * @param name key name
 * @see ByteKey
 */
fun byteCacheKey(name: String): ByteKey = ByteKey(name)

/**
 * A class for saving an item of type [Byte] to the cache.
 *
 * @param name key name
 * @see Key
 * @see byteCacheKey
 */
class ByteKey(name: String) : Key<Byte>(name) {
    override fun isTypeOf(valueClass: KClass<*>): Boolean = valueClass == Byte::class

    override suspend fun getFromStore(store: Store): Flow<Byte?> {
        return store.get(StoreKey(name, Int::class)).map { value ->
            value
                ?.takeIf { it in Byte.MIN_VALUE.toInt()..Byte.MAX_VALUE.toInt() }
                ?.toByte()
        }
    }

    override suspend fun saveToStore(item: Byte, store: Store) {
        store.save(StoreKey(name, Int::class), item.toInt())
    }
}
