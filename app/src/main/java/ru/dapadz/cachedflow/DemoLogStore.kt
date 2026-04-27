package ru.dapadz.cachedflow

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DemoLogStore {

    private const val MAX_ENTRIES = 180
    private val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val mutableEntries = MutableStateFlow<List<String>>(emptyList())

    val entries: StateFlow<List<String>> = mutableEntries

    @Synchronized
    fun add(source: String, message: String) {
        val timestamp = formatter.format(Date())
        val nextEntry = "$timestamp [$source] $message"
        mutableEntries.value = (listOf(nextEntry) + mutableEntries.value).take(MAX_ENTRIES)
    }

    @Synchronized
    fun clear() {
        mutableEntries.value = emptyList()
    }
}
