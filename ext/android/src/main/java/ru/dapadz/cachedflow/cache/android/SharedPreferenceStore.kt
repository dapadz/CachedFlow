package ru.dapadz.cachedflow.cache.android

import android.content.Context
import androidx.core.content.edit
import ru.dapadz.cachedflow.store.Store
import ru.dapadz.cachedflow.store.StoreKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Suppress("UNCHECKED_CAST")
class SharedPreferenceStore (context: Context): Store {

    private val sp = context.getSharedPreferences("cache", Context.MODE_PRIVATE)

    override suspend fun clear() = sp.edit { clear() }

    override suspend fun <T : Any> delete(key: StoreKey<T>) = sp.edit { remove(key.name) }

    override suspend fun <T : Any> get(key: StoreKey<T>): Flow<T?> = flow {
        val value: Any? = when (key.type) {
            String::class -> sp.getString(key.name, null)
            Int::class ->  sp.getInt(key.name, 0)
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