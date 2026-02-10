package ru.dapadz.cachedflow.utils


/**
 * Exception thrown when the cache is not properly initialized.
 */
class CacheNotInitializedException(message: String = "Cache has not been initialized.") : IllegalStateException(message)