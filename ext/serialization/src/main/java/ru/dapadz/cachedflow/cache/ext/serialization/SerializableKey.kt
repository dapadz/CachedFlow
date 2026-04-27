package ru.dapadz.cachedflow.cache.ext.serialization

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

/**
 * Creates a [SerializableCacheKey] for a `@Serializable` type using the default
 * empty [SerializersModule].
 *
 * This is the simplest entry point when the model type does not require
 * contextual or polymorphic serializers.
 *
 * @param name Unique cache entry name.
 * @return A key that stores values of type [T] as JSON strings.
 *
 * @see SerializableCacheKey
 */
inline fun <reified T : Any> serializableKey(name: String): SerializableCacheKey<T> {
    return serializableKey(name, EmptySerializersModule())
}

/**
 * Creates a [SerializableCacheKey] for a `@Serializable` type using a custom
 * [SerializersModule].
 *
 * Use this overload when the serialized model relies on contextual,
 * polymorphic, or otherwise custom serializer registration.
 *
 * @param name Unique cache entry name.
 * @param module Serializer registrations required to encode and decode [T].
 * @return A key that stores values of type [T] as JSON strings.
 *
 * @see SerializableCacheKey
 */
inline fun <reified T : Any> serializableKey(
    name: String,
    module: SerializersModule
): SerializableCacheKey<T> {
    return SerializableCacheKey(name, T::class, serializer<T>(), module)
}
