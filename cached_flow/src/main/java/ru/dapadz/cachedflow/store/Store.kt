package ru.dapadz.cachedflow.store

import kotlinx.coroutines.flow.Flow

interface Store {
    suspend fun <T: Any> get(key: StoreKey<T>): Flow<T?>
    suspend fun <T: Any> save(key: StoreKey<T>, value: T)
    suspend fun <T: Any> delete(key: StoreKey<T>)
    suspend fun clear()
}