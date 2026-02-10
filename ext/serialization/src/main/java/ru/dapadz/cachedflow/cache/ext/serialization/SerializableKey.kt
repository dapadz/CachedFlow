package ru.dapadz.cachedflow.cache.ext.serialization

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

inline fun <reified T : Any> serializableKey(name: String): SerializableCacheKey<T> {
    return serializableKey(name, EmptySerializersModule())
}

inline fun <reified T : Any> serializableKey(
    name: String,
    module: SerializersModule
): SerializableCacheKey<T> {
    return SerializableCacheKey(name, T::class, serializer<T>(), module)
}