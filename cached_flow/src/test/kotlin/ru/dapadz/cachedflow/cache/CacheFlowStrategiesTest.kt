package ru.dapadz.cachedflow.cache

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import ru.dapadz.cachedflow.cache.keys.stringCacheKey
import ru.dapadz.cachedflow.cache.strategy.CacheStrategyType
import ru.dapadz.cachedflow.testsupport.InMemoryStore
import ru.dapadz.cachedflow.testsupport.SilentLogger
import ru.dapadz.cachedflow.testsupport.resetCache

class CacheFlowStrategiesTest : FunSpec({

    lateinit var store: InMemoryStore

    beforeTest {
        resetCache()
        store = InMemoryStore()
        Cache.initialize(store, SilentLogger)
    }

    afterTest {
        resetCache()
    }

    test("ONLY_REQUEST returns upstream values and stores them in cache") {
        val key = stringCacheKey("user")

        val result = flowOf("fresh-value")
            .cache(key, CacheStrategyType.ONLY_REQUEST)
            .toList()

        result shouldContainExactly listOf("fresh-value")
        store.readRaw("user", String::class) shouldBe "fresh-value"
        store.saveCalls.map { it.value } shouldContainExactly listOf("fresh-value")
    }

    test("ONLY_REQUEST can skip cache writes when cachedAfterLoad is false") {
        val key = stringCacheKey("user")

        val result = flowOf("fresh-value")
            .cache(key, CacheStrategyType.ONLY_REQUEST, cachedAfterLoad = false)
            .toList()

        result shouldContainExactly listOf("fresh-value")
        store.readRaw("user", String::class) shouldBe null
        store.saveCalls shouldBe emptyList()
    }

    test("IF_HAVE emits cached value first and then refreshes it from upstream") {
        val key = stringCacheKey("user")
        store.putRaw("user", String::class, "cached-value")

        val result = flow {
            delay(25)
            emit("fresh-value")
        }.cache(key, CacheStrategyType.IF_HAVE).toList()

        result shouldContainExactly listOf("cached-value", "fresh-value")
        store.readRaw("user", String::class) shouldBe "fresh-value"
    }

    test("ONLY_CACHE serves cached data without collecting upstream flow") {
        val key = stringCacheKey("user")
        var upstreamCollected = false
        store.putRaw("user", String::class, "cached-value")

        val result = flow {
            upstreamCollected = true
            emit("fresh-value")
        }.cache(key, CacheStrategyType.ONLY_CACHE).toList()

        result shouldContainExactly listOf("cached-value")
        upstreamCollected shouldBe false
    }

    test("clear delegates to the configured store") {
        store.putRaw("user", String::class, "cached-value")

        Cache.clear()

        store.readRaw("user", String::class) shouldBe null
        store.clearCalls shouldBe 1
    }
})
