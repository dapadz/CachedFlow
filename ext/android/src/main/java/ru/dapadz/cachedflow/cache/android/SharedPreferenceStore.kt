package ru.dapadz.cachedflow.cache.android

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.dapadz.cachedflow.cache.Cache
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey

/**
 * Android [Store] implementation backed by `SharedPreferences`.
 *
 * The store keeps all cache entries in a dedicated preference file named `cache`
 * and supports the primitive value types that `SharedPreferences` can persist
 * directly: `String`, `Int`, `Boolean`, `Float`, and `Long`.
 *
 * This makes it a convenient default store for Android applications. Complex
 * objects can also be cached when they are converted to strings by a custom key
 * implementation, such as the serializers provided by the `ext:serialization`
 * module.
 *
 * ### Example
 * ```kotlin
 * Cache.initialize(
 *     store = SharedPreferenceStore(applicationContext),
 *     logger = AndroidLogger()
 * )
 * ```
 *
 * @param context Any Android [Context]. For app-wide cache setup, the
 * application context is recommended.
 *
 * @see Cache.initialize
 * @see AndroidLogger
 */
@Suppress("UNCHECKED_CAST")
class SharedPreferenceStore(context: Context) : Store {

    // Keep CachedFlow values isolated from the app's own preference files.
    private val sp = context.getSharedPreferences("cache", Context.MODE_PRIVATE)

    override suspend fun clear() = sp.edit { clear() }

    override suspend fun <T : Any> delete(key: StoreKey<T>) = sp.edit { remove(key.name) }

    override suspend fun <T : Any> get(key: StoreKey<T>): Flow<T?> = flow {
        if (!sp.contains(key.name)) {
            emit(null)
            return@flow
        }

        // SharedPreferences exposes a separate accessor per primitive type,
        // so StoreKey.type tells us which value reader should be used.
        val value: Any? = when (key.type) {
            String::class -> sp.getString(key.name, null)
            Int::class -> sp.getInt(key.name, 0)
            Boolean::class -> sp.getBoolean(key.name, false)
            Float::class -> sp.getFloat(key.name, 0f)
            Long::class -> sp.getLong(key.name, 0L)
            else -> throw IllegalArgumentException(
                "Unsupported type for get: ${key.type} for key ${key.name}. Ensure key.type matches value type."
            )
        }
        emit(value as? T)
    }

    override suspend fun <T : Any> save(key: StoreKey<T>, value: T) {
        sp.edit {
            // Unsupported objects should be encoded by a higher-level Key
            // implementation before they reach the store.
            when (value) {
                is String -> putString(key.name, value)
                is Int -> putInt(key.name, value)
                is Boolean -> putBoolean(key.name, value)
                is Float -> putFloat(key.name, value)
                is Long -> putLong(key.name, value)
                else -> throw IllegalArgumentException(
                    "Unsupported type for save: ${value::class} for key ${key.name}. Ensure key.type matches value type."
                )
            }
        }
    }
}
