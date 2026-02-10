package ru.dapadz.cachedflow.cache.strategy

import ru.dapadz.cachedflow.cache.keys.Key
import kotlinx.coroutines.flow.Flow
import ru.dapadz.cachedflow.cache.Cache
import ru.dapadz.cachedflow.utils.filterNotNull


/**
 * Will return only the cache data,
 * if the cache has not been written, it will return
 * [RuntimeException]
 */
class GetCacheOnly<T>(
    key: Key<T>,
    cachedAfterLoad: Boolean
) : CacheStrategy<T>(key, cachedAfterLoad) {
    override suspend fun execute(currentFlow: Flow<T>): Flow<T> = Cache.getFromCache(key).filterNotNull()
}