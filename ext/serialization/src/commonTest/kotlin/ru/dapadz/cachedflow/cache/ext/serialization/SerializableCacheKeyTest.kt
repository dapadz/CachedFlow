package ru.dapadz.cachedflow.cache.ext.serialization

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import ru.dapadz.cachedflow.cache.Cache
import ru.dapadz.cachedflow.cache.cache
import ru.dapadz.cachedflow.cache.strategy.CacheStrategyType
import ru.dapadz.cachedflow.logger.Logger
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlin.reflect.KClass

class SerializableCacheKeyTest : FunSpec({

    lateinit var store: TestStore

    beforeTest {
        store = TestStore()
        Cache.initialize(store, SilentLogger)
    }

    test("serializableKey round-trips through the cache") {
        val key = serializableKey<Profile>("profile")
        val profile = Profile(id = 7, name = "Ada")

        flowOf(profile)
            .cache(key, CacheStrategyType.ONLY_REQUEST)
            .toList()

        val cached = emptyFlow<Profile>()
            .cache(key, CacheStrategyType.ONLY_CACHE)
            .toList()

        cached shouldContainExactly listOf(profile)
    }

    test("malformed JSON is treated as a cache miss") {
        store.save(StoreKey("broken", String::class), """{"id":1,"name":"Ada""")
        val key = serializableKey<Profile>("broken")

        val cached = emptyFlow<Profile>()
            .cache(key, CacheStrategyType.ONLY_CACHE)
            .toList()

        cached shouldBe emptyList()
    }
})

@Serializable
private data class Profile(
    val id: Int,
    val name: String
)

private object SilentLogger : Logger {
    override fun info(tag: String, message: String) = Unit
    override fun error(tag: String, message: String) = Unit
}

private class TestStore : Store {

    private val values = linkedMapOf<StoredEntryKey, Any>()

    override suspend fun clear() {
        values.clear()
    }

    override suspend fun <T : Any> delete(key: StoreKey<T>) {
        values.remove(StoredEntryKey(key.name, key.type))
    }

    override suspend fun <T : Any> get(key: StoreKey<T>): Flow<T?> {
        @Suppress("UNCHECKED_CAST")
        return flowOf(values[StoredEntryKey(key.name, key.type)] as T?)
    }

    override suspend fun <T : Any> save(key: StoreKey<T>, value: T) {
        values[StoredEntryKey(key.name, key.type)] = value
    }
}

private data class StoredEntryKey(
    val name: String,
    val type: KClass<*>
)
