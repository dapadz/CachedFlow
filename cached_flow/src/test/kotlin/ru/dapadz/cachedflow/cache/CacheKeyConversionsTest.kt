package ru.dapadz.cachedflow.cache

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.first
import ru.dapadz.cachedflow.cache.keys.byteCacheKey
import ru.dapadz.cachedflow.cache.keys.charCacheKey
import ru.dapadz.cachedflow.cache.keys.doubleCacheKey
import ru.dapadz.cachedflow.cache.keys.shortCacheKey
import ru.dapadz.cachedflow.testsupport.InMemoryStore

class CacheKeyConversionsTest : FunSpec({

    test("ByteKey round-trips arbitrary byte values") {
        checkAll(Arb.byte()) { value ->
            val store = InMemoryStore()
            val key = byteCacheKey("byte")

            key.saveToStore(value, store)
            key.getFromStore(store).first() shouldBe value
            store.readRaw("byte", Int::class) shouldBe value.toInt()
        }
    }

    test("ByteKey returns null when stored integer is out of byte range") {
        val store = InMemoryStore()
        val key = byteCacheKey("byte")
        store.putRaw("byte", Int::class, Byte.MAX_VALUE.toInt() + 1)

        key.getFromStore(store).first() shouldBe null
    }

    test("ShortKey returns null when stored integer is out of short range") {
        val store = InMemoryStore()
        val key = shortCacheKey("short")
        store.putRaw("short", Int::class, Short.MIN_VALUE.toInt() - 1)

        key.getFromStore(store).first() shouldBe null
    }

    listOf(
        "A" to 'A',
        "ab" to null,
        "" to null,
        null to null
    ).forEach { (rawValue, expected) ->
        test("CharKey decodes ${rawValue ?: "null"} into $expected") {
            val store = InMemoryStore()
            val key = charCacheKey("char")
            if (rawValue != null) {
                store.putRaw("char", String::class, rawValue)
            }

            key.getFromStore(store).first() shouldBe expected
        }
    }

    test("DoubleKey preserves exact bit representation") {
        val store = InMemoryStore()
        val key = doubleCacheKey("double")
        val value = -0.0

        key.saveToStore(value, store)

        val restored = key.getFromStore(store).first()
        restored?.toBits() shouldBe value.toBits()
        store.readRaw("double", Long::class) shouldBe value.toBits()
    }
})
