package ru.dapadz.cachedflow.cache.ext.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

inline fun <reified E : Any> serializableListKey(
    name: String,
    module: SerializersModule
): SerializableCacheKey<List<E>> {
    val listSerializer: KSerializer<List<E>> = ListSerializer(serializer<E>())
    @Suppress("UNCHECKED_CAST")
    val kclass = List::class as KClass<List<E>>
    return SerializableCacheKey(name, kclass, listSerializer, module)
}

inline fun <reified E : Any> serializableListKey(name: String): SerializableCacheKey<List<E>> {
    return serializableListKey(name, EmptySerializersModule())
}