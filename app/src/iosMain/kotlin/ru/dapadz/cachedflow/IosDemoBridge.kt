package ru.dapadz.cachedflow

import kotlinx.coroutines.runBlocking
import ru.dapadz.cachedflow.cache.strategy.CacheStrategyType

data class IosDemoSnapshot(
    val platformName: String,
    val storeLabel: String,
    val overview: String,
    val strategyStatus: String,
    val cacheDump: String,
    val logs: String
)

class IosDemoBridge {

    private val environment = createIosDemoEnvironment()
    private val runner = DemoScenarioRunner(environment)

    fun snapshot(): IosDemoSnapshot = buildSnapshot()

    fun clearCache(): IosDemoSnapshot = runBlocking {
        runner.clearCache()
        buildSnapshot()
    }

    fun clearLog(): IosDemoSnapshot {
        runner.clearLog()
        return buildSnapshot()
    }

    fun runOnlyRequest(cachedAfterLoad: Boolean): IosDemoSnapshot = runStrategy(
        type = CacheStrategyType.ONLY_REQUEST,
        cachedAfterLoad = cachedAfterLoad
    )

    fun runIfHave(cachedAfterLoad: Boolean): IosDemoSnapshot = runStrategy(
        type = CacheStrategyType.IF_HAVE,
        cachedAfterLoad = cachedAfterLoad
    )

    fun runOnlyCache(cachedAfterLoad: Boolean): IosDemoSnapshot = runStrategy(
        type = CacheStrategyType.ONLY_CACHE,
        cachedAfterLoad = cachedAfterLoad
    )

    fun runSerializableObject(): IosDemoSnapshot = runAction {
        runner.serializationActions()[0].run()
    }

    fun runSerializableList(): IosDemoSnapshot = runAction {
        runner.serializationActions()[1].run()
    }

    fun runPolymorphicList(): IosDemoSnapshot = runAction {
        runner.serializationActions()[2].run()
    }

    fun runJsonResilience(): IosDemoSnapshot = runAction {
        runner.serializationActions()[3].run()
    }

    fun runStringRoundTrip(): IosDemoSnapshot = runAction {
        runner.primitiveActions()[0].run()
    }

    fun runIntRoundTrip(): IosDemoSnapshot = runAction {
        runner.primitiveActions()[1].run()
    }

    fun runBooleanRoundTrip(): IosDemoSnapshot = runAction {
        runner.primitiveActions()[8].run()
    }

    private fun runStrategy(
        type: CacheStrategyType,
        cachedAfterLoad: Boolean
    ): IosDemoSnapshot = runAction {
        runner.runStrategy(type, cachedAfterLoad)
    }

    private fun runAction(
        block: suspend () -> Unit
    ): IosDemoSnapshot = runBlocking {
        block()
        buildSnapshot()
    }

    private fun buildSnapshot(): IosDemoSnapshot {
        val uiState = runner.uiState.value
        val logs = runner.logs.value.joinToString("\n")
        return IosDemoSnapshot(
            platformName = environment.platformName,
            storeLabel = environment.storeLabel,
            overview = DemoScreenText.overview(
                platformName = environment.platformName,
                storeLabel = environment.storeLabel
            ),
            strategyStatus = uiState.strategyStatus,
            cacheDump = uiState.cacheDump,
            logs = if (logs.isEmpty()) DemoScreenText.emptyLog else logs
        )
    }
}
