package ru.dapadz.cachedflow.cache.ext.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Creates a [SerializableCacheKey] for a list of `@Serializable` elements using
 * a custom [SerializersModule].
 *
 * This is useful when a cached collection contains types that rely on
 * contextual or polymorphic serializers.
 *
 * @param name Unique cache entry name.
 * @param module Serializer registrations required for list elements.
 * @return A key that stores `List<E>` values as JSON strings.
 *
 * @see SerializableCacheKey
 */
inline fun <reified E : Any> serializableListKey(
    name: String,
    module: SerializersModule
): SerializableCacheKey<List<E>> {
    val listSerializer: KSerializer<List<E>> = ListSerializer(serializer<E>())
    @Suppress("UNCHECKED_CAST")
    // Kotlin reflection erases the element type, so List::class is the closest
    // runtime representation we can provide for List<E>.
    val kclass = List::class as KClass<List<E>>
    return SerializableCacheKey(name, kclass, listSerializer, module)
}

/**
 * Creates a [SerializableCacheKey] for a list of `@Serializable` elements using
 * the default empty [SerializersModule].
 *
 * @param name Unique cache entry name.
 * @return A key that stores `List<E>` values as JSON strings.
 *
 * @see SerializableCacheKey
 */
inline fun <reified E : Any> serializableListKey(name: String): SerializableCacheKey<List<E>> {
    return serializableListKey(name, EmptySerializersModule())
}
