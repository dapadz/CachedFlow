package ru.dapadz.cachedflow.cache

import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.cache.keys.stringCacheKey
import ru.dapadz.cachedflow.cache.keys.Key
import ru.dapadz.cachedflow.logger.DefaultLogger
import ru.dapadz.cachedflow.logger.Logger
import ru.dapadz.cachedflow.utils.CacheNotInitializedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach


/**
 * Singleton object for handling query caching using [Flow].
 *
 * This cache stores key-value pairs using a pluggable [Store] implementation, which you must provide
 * for your platform (e.g., file system, shared preferences, in-memory, etc.).
 * Each cache entry is identified by a custom [Key], which defines how data is saved and retrieved.
 *
 * The [Cache] object is platform-agnostic and can be initialized in any environment by passing
 * appropriate implementations of [Store] and [Logger] (optional).
 *
 * ### Key Concepts
 * - **Store**: Defines how data is persisted (save, retrieve, delete, clear).
 * - **Key**: Defines the type and strategy for accessing specific cached entries.
 * - **Logger**: Optional logger for tracking cache operations (defaults to [DefaultLogger]).
 *
 * ### Public API
 * - [initialize]: Must be called once before any caching operations.
 * - [clear]: Clears all cached entries.
 *
 * ### Example Usage
 * ```kotlin
 * fun main() {
 *     // Provide an implementation of the Store interface for your platform.
 *     val store: Store = MyMultiplatformStore()
 *     Cache.initialize(store)
 *
 *     // Example of using a key to store/retrieve data
 *     val key = stringCacheKey("user_profile")
 *     val request = repository.getProfile()
 *         .cache(key)
 * }
 * ```
 *
 * @see cache
 * @see stringCacheKey
 * @see Key
 */
object Cache {

    private const val TAG = "CacheFlow"

    private var store: Store? = null
    private var logger: Logger? = null

    /**
     * Initializes the cache system with a [Store] and optional [Logger].
     *
     * This method **must** be called before any other cache operations.
     * If [logger] is not provided, a [DefaultLogger] will be used.
     *
     * @param store Implementation of the storage mechanism.
     * @param logger Optional implementation of logging behavior.
     */
    fun initialize(
        store: Store,
        logger: Logger = DefaultLogger()
    ) {
        this.store = store
        this.logger = logger
        requireLogger().info(TAG, "Cache library initialize")
    }

    /**
     * Clears all stored key-value pairs from the cache.
     *
     * This calls [Store.clear], which should wipe all cached entries.
     * The method is `suspend` since the underlying store operation may be asynchronous.
     *
     * @throws CacheNotInitializedException if [initialize] has not been called.
     */
    suspend fun clear() {
        requireStore().clear()
        requireLogger().info(TAG, "Cache clear")
    }

    /**
     * Retrieves a value of type [T] associated with the given [Key].
     *
     * Internally, this delegates to [Key.getFromStore], and emits the result via [Flow].
     * This method is typically used in reactive environments to observe cached values.
     *
     * @param key A specific key describing how to retrieve the value.
     * @return A [Flow] emitting a nullable value of type [T].
     * @throws CacheNotInitializedException if [initialize] has not been called.
     */
    internal fun <T: Any?> getFromCache(key: Key<T>): Flow<T?> = flow {
        emitAll(key.getFromStore(requireStore()))
    }.onEach {
        requireLogger().info(TAG, "<-- Cache get: $it")
    }

    /**
     * Saves a value [item] into the cache using the specified [Key].
     *
     * Internally calls [Key.saveToStore], which delegates to the [Store] implementation.
     *
     * @param key A key describing how the data should be saved.
     * @param item The value to be cached.
     * @throws CacheNotInitializedException if [initialize] has not been called.
     */
    internal suspend fun <T> saveToCache(key : Key<T>, item: T) {
        key.saveToStore(item, requireStore())
        requireLogger().info(TAG, "--> Cache save: $item")
    }

    /**
     * Returns the currently initialized [Store] instance.
     *
     * Used internally to enforce that the [store] has been initialized.
     *
     * @return A non-null [Store] instance.
     * @throws CacheNotInitializedException if [initialize] has not been called.
     */
    private fun requireStore(): Store = store ?: throw CacheNotInitializedException()

    /**
     * Returns the currently initialized [Logger] instance.
     *
     * Used internally to provide consistent logging behavior across operations.
     *
     * @return A non-null [Logger] instance.
     * @throws CacheNotInitializedException if [initialize] has not been called.
     */
    private fun requireLogger(): Logger = logger ?: throw CacheNotInitializedException()
}