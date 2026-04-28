package ru.dapadz.cachedflow

import kotlin.time.TimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DemoLogStore {

    private val startedAt = TimeSource.Monotonic.markNow()
    private val mutableEntries = MutableStateFlow<List<String>>(emptyList())

    val entries: StateFlow<List<String>> = mutableEntries.asStateFlow()

    fun add(source: String, message: String) {
        val elapsedMillis = startedAt.elapsedNow().inWholeMilliseconds
        val nextEntry = "${formatElapsed(elapsedMillis)} [$source] $message"
        mutableEntries.value = (listOf(nextEntry) + mutableEntries.value).take(MAX_ENTRIES)
    }

    fun clear() {
        mutableEntries.value = emptyList()
    }

    private fun formatElapsed(elapsedMillis: Long): String {
        val minutes = elapsedMillis / 60_000
        val seconds = (elapsedMillis / 1_000) % 60
        val millis = elapsedMillis % 1_000
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}.${millis.toString().padStart(3, '0')}"
    }

    private companion object {
        const val MAX_ENTRIES = 180
    }
}
