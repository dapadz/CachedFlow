package ru.dapadz.cachedflow.cache

import ru.dapadz.cachedflow.cache.keys.Key
import ru.dapadz.cachedflow.cache.keys.stringCacheKey
import ru.dapadz.cachedflow.cache.strategy.CacheStrategy
import ru.dapadz.cachedflow.cache.strategy.CacheStrategyType
import ru.dapadz.cachedflow.cache.strategy.GetCacheOnly
import ru.dapadz.cachedflow.cache.strategy.GetIfCacheAvailable
import ru.dapadz.cachedflow.cache.strategy.GetRequestOnly
import kotlinx.coroutines.flow.Flow

/**
 * Extension function for [Flow] that adds caching logic.
 *
 * Depending on the selected [CacheStrategyType], this function either:
 * - Retrieves data from the cache if available,
 * - Uses only the cache and throws an error if no data exists,
 * - Or always skips the cache and directly collects from the original [Flow].
 *
 * You can also control whether to store the collected result back into the cache
 * using the [cachedAfterLoad] flag.
 *
 * @param key The caching key associated with the value, must extend from [Key]. Example: [stringCacheKey].
 * @param type The caching strategy to use, defined in [CacheStrategyType]. Defaults to [CacheStrategyType.IF_HAVE].
 * @param cachedAfterLoad Whether to save the result into cache after loading it from the original [Flow].
 *
 * @return A [Flow] with caching behavior applied.
 *
 * @see Cache
 * @see Key
 * @see CacheStrategyType
 */
suspend fun <T> Flow<T>.cache(
    key: Key<T>,
    type: CacheStrategyType = CacheStrategyType.IF_HAVE,
    cachedAfterLoad: Boolean = true
): Flow<T> {
    val strategy = when (type) {
        CacheStrategyType.IF_HAVE -> GetIfCacheAvailable(key, cachedAfterLoad)
        CacheStrategyType.ONLY_CACHE -> GetCacheOnly(key, cachedAfterLoad)
        CacheStrategyType.ONLY_REQUEST -> GetRequestOnly(key, cachedAfterLoad)
    }
    return cache(key, strategy, cachedAfterLoad)
}

/**
 * Extension function for [Flow] that uses a custom [CacheStrategy] implementation
 * to determine how caching should be applied to the flow.
 *
 * This gives full control over how the cache is used or updated.
 *
 * @param key The cache key associated with this flow.
 * @param strategy A concrete implementation of [CacheStrategy] to use.
 * @param cachedAfterLoad Whether the result should be saved back to the cache after collection.
 *
 * @return A [Flow] transformed according to the provided strategy.
 */
suspend fun <T> Flow<T>.cache(
    key: Key<T>,
    strategy: CacheStrategy<T>,
    cachedAfterLoad: Boolean = true
): Flow<T> = strategy.execute(this)