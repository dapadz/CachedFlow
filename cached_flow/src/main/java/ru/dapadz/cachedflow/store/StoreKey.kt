package ru.dapadz.cachedflow.store

import kotlin.reflect.KClass

class StoreKey<T : Any>(
    val name: String,
    val type: KClass<T>
)