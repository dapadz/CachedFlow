package ru.dapadz.cachedflow.cache.keys

import ru.dapadz.cachedflow.store.Store
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass


/**
 * An abstract key class that contains basic methods for
 * working with cache keys.
 *
 * Used to create the key behavior in certain situations, such as saving or receiving
 * data from the cache.
 *
 * All keys must be inherited from this class, for example [stringCacheKey].
 * The class contains several abstract functions.
 * - [Key.isTypeOf]
 * - [Key.getFromStore]
 * - [Key.saveToStore]
 *
 * All these functions must be implemented in the successor class
 *
 * @see stringCacheKey
 */
abstract class Key<T>(val name: String) {

    /**
     * A method that compares the types of the object being sent from the cache.
     *
     * ```
     * fun isTypeOf(valueClass: KClass<*>): Boolean = valueClass == String::class
     * ```
     *
     * @param valueClass a class of type [KClass] to be compared with the type from the key
     */
    abstract fun isTypeOf(valueClass: KClass<*>): Boolean

    /**
     * A method that implements saving [item] of type [T] to the cache.
     *
     * ```
     * override suspend fun saveToStore(item: Int, store: Store) = store.save(StoreKey(name, Int::class), item)
     * ```
     *
     * @param item is an object of type [T] that needs to be saved to the cache.
     * @param store cached data storage
     */
    abstract suspend fun saveToStore(item: T, store: Store)

    /**
     * Method for getting item[T] from cache
     *
     * ```
     * override suspend fun getFromStore(store: Store): Flow<Int?> = store.get(StoreKey(name, Int::class))
     * ```
     * @param store cached data storage
     * @return object of type [T] from cache
     */
    abstract suspend fun getFromStore(store: Store): Flow<T?>
}