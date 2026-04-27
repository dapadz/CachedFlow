package ru.dapadz.cachedflow

internal object DemoScreenText {

    val overview = """
        This playground covers:
        • cached_flow: Cache.initialize, Cache.clear, IF_HAVE, ONLY_REQUEST, ONLY_CACHE, and cachedAfterLoad switching.
        • primitive keys: String, Int, Long, Float, Double, Byte, Short, Char, Boolean.
        • ext/android: SharedPreferenceStore as the backend and AndroidLogger via DemoLogger.
        • ext/serialization: serializableKey, serializableListKey, SerializersModule, tolerant decoding with ignoreUnknownKeys, and cache misses for malformed JSON.
    """.trimIndent()

    const val initialStrategyStatus =
        "Ready to run. For IF_HAVE, populate the cache first or just tap the button twice."

    const val emptyStrategyResult =
        "No emissions. For ONLY_CACHE, this means the library currently treats an empty cache as a cache miss without throwing."

    const val emptyLog =
        "The log is empty for now. Once you run scenarios, both demo actions and internal Cache messages will appear here."

    fun strategyResult(emissionsCount: Int, lastValue: String): String {
        return "Emissions: $emissionsCount. Last value: $lastValue"
    }

    fun emptyCacheDump(): String = "SharedPreferences \"$CACHE_PREFS_NAME\" is empty."
}
