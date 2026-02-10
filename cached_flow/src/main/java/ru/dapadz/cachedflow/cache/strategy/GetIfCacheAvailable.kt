package ru.dapadz.cachedflow.cache.strategy

import ru.dapadz.cachedflow.cache.Cache
import ru.dapadz.cachedflow.cache.keys.Key
import ru.dapadz.cachedflow.utils.filterNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

/**
 * A strategy for obtaining caching if the cache was previously saved.
 * Returns new data from flow and stores new values in the cache if [cachedAfterLoad] == true.
 */
class GetIfCacheAvailable<T>(
    key: Key<T>,
    cachedAfterLoad: Boolean
) : CacheStrategy<T>(key, cachedAfterLoad) {

    override suspend fun execute(currentFlow: Flow<T>): Flow<T> = merge(
        Cache.getFromCache(key).filterNotNull(),
        currentFlow
    ).onEach {
        if (cachedAfterLoad)
        {
            Cache.saveToCache(key, it)
        }
    }
}