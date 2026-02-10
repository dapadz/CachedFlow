package ru.dapadz.cachedflow.cache.strategy

/**
 * Available caching execution strategies
 *
 * @property IF_HAVE
 * @property ONLY_CACHE
 * @property ONLY_REQUEST
 *
 * @see CacheStrategy
 */
enum class CacheStrategyType {

    /**
     * will return the cache only if it has been saved,
     * then it will return the received data
     * @see GetIfCacheAvailable
     */
    IF_HAVE,

    /**
     * Will return only the cache data,
     * if the cache has not been written, it will return [RuntimeException]
     * @see GetCacheOnly
     */
    ONLY_CACHE,

    /**
     * Will return only the request data
     * @see GetRequestOnly
     */
    ONLY_REQUEST
}