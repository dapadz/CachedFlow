package ru.dapadz.cachedflow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import ru.dapadz.cachedflow.cache.Cache
import ru.dapadz.cachedflow.cache.cache
import ru.dapadz.cachedflow.cache.ext.serialization.serializableKey
import ru.dapadz.cachedflow.cache.ext.serialization.serializableListKey
import ru.dapadz.cachedflow.cache.keys.Key
import ru.dapadz.cachedflow.cache.keys.booleanCacheKey
import ru.dapadz.cachedflow.cache.keys.byteCacheKey
import ru.dapadz.cachedflow.cache.keys.charCacheKey
import ru.dapadz.cachedflow.cache.keys.doubleCacheKey
import ru.dapadz.cachedflow.cache.keys.floatCacheKey
import ru.dapadz.cachedflow.cache.keys.integerCacheKey
import ru.dapadz.cachedflow.cache.keys.longCacheKey
import ru.dapadz.cachedflow.cache.keys.shortCacheKey
import ru.dapadz.cachedflow.cache.keys.stringCacheKey
import ru.dapadz.cachedflow.cache.strategy.CacheStrategyType

internal class DemoScenarioRunner(
    private val environment: DemoEnvironment
) {

    private val logStore = DemoLogStore()
    private val strategyKey = stringCacheKey("demo.strategy.message")
    private val polymorphicModule = SerializersModule {
        polymorphic(DemoAnimal::class) {
            subclass(DemoDog::class)
            subclass(DemoCat::class)
        }
    }

    private val mutableUiState = MutableStateFlow(
        DemoUiState(
            strategyStatus = DemoScreenText.initialStrategyStatus,
            cacheDump = buildCacheDump()
        )
    )

    val logs: StateFlow<List<String>> = logStore.entries
    val uiState: StateFlow<DemoUiState> = mutableUiState.asStateFlow()

    private var requestCounter = 0

    init {
        Cache.initialize(
            store = environment.store,
            logger = DemoLogger(logStore, environment.logger)
        )
        logStore.add(
            "App",
            "Cache initialized for ${environment.platformName} using ${environment.storeLabel}"
        )
        refreshCacheDump()
    }

    fun primitiveActions(): List<DemoActionSpec> = listOf(
        primitiveAction(
            title = "String key",
            label = "String",
            key = stringCacheKey("demo.primitive.string")
        ) {
            "token-${nextCounter()}"
        },
        primitiveAction(
            title = "Int key",
            label = "Int",
            key = integerCacheKey("demo.primitive.int")
        ) {
            nextCounter() * 10
        },
        primitiveAction(
            title = "Long key",
            label = "Long",
            key = longCacheKey("demo.primitive.long")
        ) {
            nextCounter().toLong() * 1_000L
        },
        primitiveAction(
            title = "Float key",
            label = "Float",
            key = floatCacheKey("demo.primitive.float")
        ) {
            nextCounter() + 0.75f
        },
        primitiveAction(
            title = "Double key",
            label = "Double",
            key = doubleCacheKey("demo.primitive.double")
        ) {
            nextCounter() + 0.125
        },
        primitiveAction(
            title = "Byte key",
            label = "Byte",
            key = byteCacheKey("demo.primitive.byte")
        ) {
            (nextCounter() % 120).toByte()
        },
        primitiveAction(
            title = "Short key",
            label = "Short",
            key = shortCacheKey("demo.primitive.short")
        ) {
            (nextCounter() * 7).toShort()
        },
        primitiveAction(
            title = "Char key",
            label = "Char",
            key = charCacheKey("demo.primitive.char")
        ) {
            ('A'.code + nextCounter() % 26).toChar()
        },
        primitiveAction(
            title = "Boolean key",
            label = "Boolean",
            key = booleanCacheKey("demo.primitive.boolean")
        ) {
            nextCounter() % 2 == 0
        }
    )

    fun serializationActions(): List<DemoActionSpec> = listOf(
        action("Serializable object") {
            runSerializableObjectScenario()
        },
        action("Serializable list") {
            runSerializableListScenario()
        },
        action("Polymorphic list") {
            runPolymorphicListScenario()
        },
        action("JSON resilience") {
            runJsonResilienceScenario()
        }
    )

    suspend fun clearCache() {
        runScenario("Clear cache") {
            Cache.clear()
            logStore.add("App", "Cache.clear() finished")
        }
    }

    fun clearLog() {
        logStore.clear()
    }

    suspend fun runStrategy(
        strategy: CacheStrategyType,
        cachedAfterLoad: Boolean
    ) {
        runScenario(strategy.name) {
            val emissions = collectScenario(
                label = "strategy/$strategy (cachedAfterLoad=$cachedAfterLoad)",
                flow = fakeRequestFlow().cache(
                    key = strategyKey,
                    type = strategy,
                    cachedAfterLoad = cachedAfterLoad
                )
            )
            updateStrategyStatus(emissions)
        }
    }

    private fun action(
        title: String,
        style: DemoButtonStyle = DemoButtonStyle.Outlined,
        block: suspend () -> Unit
    ): DemoActionSpec {
        return DemoActionSpec(title = title, style = style) {
            runScenario(title, block)
        }
    }

    private fun <T> primitiveAction(
        title: String,
        label: String,
        key: Key<T>,
        sample: () -> T
    ): DemoActionSpec {
        return action(title = title) {
            runPrimitiveRoundTrip(label = label, key = key, sample = sample())
        }
    }

    private suspend fun runScenario(
        title: String,
        block: suspend () -> Unit
    ) {
        logStore.add("Scenario", "Start: $title")
        try {
            block()
            logStore.add("Scenario", "Done: $title")
        } catch (throwable: Throwable) {
            val label = throwable.message ?: throwable::class.simpleName ?: "Unknown error"
            logStore.add("Scenario", "Failed: $title -> $label")
            throw throwable
        } finally {
            refreshCacheDump()
        }
    }

    private suspend fun <T> runPrimitiveRoundTrip(
        label: String,
        key: Key<T>,
        sample: T
    ) {
        collectScenario(
            label = "$label/save",
            flow = flowOf(sample).cache(key, CacheStrategyType.ONLY_REQUEST)
        )
        collectScenario(
            label = "$label/read",
            flow = emptyFlow<T>().cache(key, CacheStrategyType.ONLY_CACHE)
        )
    }

    private suspend fun runSerializableObjectScenario() {
        val profile = DemoProfile(
            id = nextCounter(),
            name = "Profile #$requestCounter",
            badge = if (requestCounter % 2 == 0) "cached" else "fresh",
            active = requestCounter % 2 == 0
        )
        val key = serializableKey<DemoProfile>("demo.serialization.profile")
        collectScenario("serializable-object/save", flowOf(profile).cache(key, CacheStrategyType.ONLY_REQUEST))
        collectScenario("serializable-object/read", emptyFlow<DemoProfile>().cache(key, CacheStrategyType.ONLY_CACHE))
    }

    private suspend fun runSerializableListScenario() {
        val list = listOf(
            DemoArticle(id = nextCounter(), title = "Intro to cached_flow", weight = 0.35f),
            DemoArticle(id = nextCounter(), title = "Shared cache reuse", weight = 0.72f),
            DemoArticle(id = nextCounter(), title = "Serialization extension", weight = 0.98f)
        )
        val key = serializableListKey<DemoArticle>("demo.serialization.article.list")
        collectScenario("serializable-list/save", flowOf(list).cache(key, CacheStrategyType.ONLY_REQUEST))
        collectScenario("serializable-list/read", emptyFlow<List<DemoArticle>>().cache(key, CacheStrategyType.ONLY_CACHE))
    }

    private suspend fun runPolymorphicListScenario() {
        val animals: List<DemoAnimal> = listOf(
            DemoDog(name = "Bolt-${nextCounter()}", isGoodBoy = true),
            DemoCat(name = "Pixel-${nextCounter()}", livesLeft = 7)
        )
        val key = serializableListKey<DemoAnimal>(
            name = "demo.serialization.animal.list",
            module = polymorphicModule
        )
        collectScenario("polymorphic-list/save", flowOf(animals).cache(key, CacheStrategyType.ONLY_REQUEST))
        collectScenario("polymorphic-list/read", emptyFlow<List<DemoAnimal>>().cache(key, CacheStrategyType.ONLY_CACHE))
    }

    private suspend fun runJsonResilienceScenario() {
        environment.store.putRawString(
            "demo.serialization.compatible.profile",
            """{"id":42,"name":"Legacy profile","badge":"migrated","active":true,"unusedField":"ignored"}"""
        )
        environment.store.putRawString(
            "demo.serialization.broken.profile",
            """{"id":7,"name":"Broken"""
        )

        logStore.add(
            "Serialization",
            "Injected JSON with unknown fields and malformed JSON straight into the backing store"
        )

        val compatibleKey = serializableKey<DemoProfile>("demo.serialization.compatible.profile")
        val brokenKey = serializableKey<DemoProfile>("demo.serialization.broken.profile")

        collectScenario("json-compatible/read", emptyFlow<DemoProfile>().cache(compatibleKey, CacheStrategyType.ONLY_CACHE))
        collectScenario("json-broken/read", emptyFlow<DemoProfile>().cache(brokenKey, CacheStrategyType.ONLY_CACHE))
    }

    private suspend fun fakeRequestFlow(): Flow<String> = flow {
        val requestId = nextCounter()
        logStore.add("API", "Fake request #$requestId started")
        delay(FAKE_REQUEST_DELAY_MS)
        emit("payload-$requestId")
        logStore.add("API", "Fake request #$requestId finished")
    }

    private suspend fun <T> collectScenario(
        label: String,
        flow: Flow<T>
    ): List<T> {
        val emissions = mutableListOf<T>()
        flow.collect { value ->
            emissions += value
            logStore.add("Result", "$label -> ${formatValue(value)}")
        }
        if (emissions.isEmpty()) {
            logStore.add("Result", "$label -> no emissions")
        }
        return emissions
    }

    private fun updateStrategyStatus(emissions: List<*>) {
        val nextText = if (emissions.isEmpty()) {
            DemoScreenText.emptyStrategyResult
        } else {
            DemoScreenText.strategyResult(
                emissionsCount = emissions.size,
                lastValue = formatValue(emissions.last())
            )
        }
        mutableUiState.update { state ->
            state.copy(strategyStatus = nextText)
        }
    }

    private fun refreshCacheDump() {
        mutableUiState.update { state ->
            state.copy(cacheDump = buildCacheDump())
        }
    }

    private fun buildCacheDump(): String {
        val entries = environment.store.dumpEntries()
        if (entries.isEmpty()) {
            return DemoScreenText.emptyCacheDump(environment.storeLabel)
        }

        return buildString {
            append(environment.storeLabel)
            appendLine()
            entries.forEach { (key, value) ->
                append("- ")
                append(key)
                append(" = ")
                appendLine(value)
            }
        }
    }

    private fun nextCounter(): Int {
        requestCounter += 1
        return requestCounter
    }

    private fun formatValue(value: Any?): String {
        return when (value) {
            is List<*> -> value.joinToString(prefix = "[", postfix = "]")
            else -> value.toString()
        }
    }
}

internal data class DemoUiState(
    val strategyStatus: String,
    val cacheDump: String
)
