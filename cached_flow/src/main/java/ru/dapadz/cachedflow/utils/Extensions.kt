package ru.dapadz.cachedflow.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

internal fun <T> Flow<T?>.filterNotNull() = this.mapNotNull { it }