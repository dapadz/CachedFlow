package ru.dapadz.cachedflow.cache.strategy

import ru.dapadz.cachedflow.cache.keys.Key
import ru.dapadz.cachedflow.cache.keys.stringCacheKey
import kotlinx.coroutines.flow.Flow

/**
 * A basic abstract class for implementing a caching strategy
 *
 * @param key the caching key in the format [Key], for example [stringCacheKey]
 * @param cachedAfterLoad need to save the new values to the cache after receiving the result
 *
 * @see Key
 */
abstract class CacheStrategy <T> (
    protected val key: Key<T>,
    protected val cachedAfterLoad : Boolean
) {
    abstract suspend fun execute(currentFlow: Flow<T>): Flow<T>
}