package ru.dapadz.cachedflow

internal object DemoScreenText {

    fun overview(platformName: String, storeLabel: String): String = """
        CachedFlow now runs from commonMain and can be used from Kotlin Multiplatform or Compose Multiplatform shared code.

        Platform: $platformName
        Store: $storeLabel

        This playground covers:
        - cached_flow: Cache.initialize, Cache.clear, IF_HAVE, ONLY_REQUEST, ONLY_CACHE, and cachedAfterLoad switching.
        - primitive keys: String, Int, Long, Float, Double, Byte, Short, Char, Boolean.
        - ext:serialization: serializableKey, serializableListKey, SerializersModule, tolerant decoding with ignoreUnknownKeys, and cache misses for malformed JSON.
        - Android target additionally exercises ext:android SharedPreferenceStore and AndroidLogger.
    """.trimIndent()

    const val initialStrategyStatus =
        "Ready to run. For IF_HAVE, populate the cache first or tap the button twice."

    const val emptyStrategyResult =
        "No emissions. For ONLY_CACHE, this means the cache is currently empty."

    const val emptyLog =
        "The log is empty for now. Run any scenario to see both demo events and internal Cache messages."

    fun strategyResult(emissionsCount: Int, lastValue: String): String {
        return "Emissions: $emissionsCount. Last value: $lastValue"
    }

    fun emptyCacheDump(storeLabel: String): String = "$storeLabel is empty."
}
