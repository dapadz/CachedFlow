package ru.dapadz.cachedflow.cache.android

import android.util.Log
import ru.dapadz.cachedflow.cache.Cache
import ru.dapadz.cachedflow.logger.Logger

/**
 * Android-specific [Logger] implementation backed by Logcat.
 *
 * Use this logger when initializing [Cache] inside an Android app to route
 * cache diagnostics to `android.util.Log`.
 *
 * ### Example
 * ```kotlin
 * Cache.initialize(
 *     store = SharedPreferenceStore(applicationContext),
 *     logger = AndroidLogger()
 * )
 * ```
 */
class AndroidLogger : Logger {
    override fun error(tag: String, message: String) {
        Log.e(tag, "⛔ $message")
    }

    override fun info(tag: String, message: String) {
        Log.i(tag, "ℹ️ $message")
    }
}
