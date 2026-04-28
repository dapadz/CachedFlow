# CachedFlow

**A lightweight caching library for Kotlin Flow with first-class Kotlin Multiplatform support.**

Starting with **`1.1.0`**, CachedFlow can be used directly from `commonMain` in Kotlin Multiplatform and Compose Multiplatform projects.

## Supported targets

| Module | Android | Desktop JVM | iOS |
| --- | --- | --- | --- |
| `cachedflow` | Yes | Yes | Yes |
| `cachedflow-ext-serialization` | Yes | Yes | Yes |
| `cachedflow-ext-android` | Yes | No | No |

The repository also includes a shared demo that exercises the library on **Android, Desktop, and iOS**.

## Features

- Typed cache keys for primitive and custom value types
- Flow-oriented cache strategies: `IF_HAVE`, `ONLY_REQUEST`, `ONLY_CACHE`
- Pluggable `Store` abstraction for platform-specific persistence
- Optional logging through `Logger`
- Kotlin Multiplatform-ready core API for shared business logic
- `kotlinx.serialization` extension for serializable objects and lists

## Installation

### Kotlin Multiplatform / Compose Multiplatform

Use CachedFlow from `commonMain`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("ru.dapadz:cachedflow:1.1.0")
            implementation("ru.dapadz:cachedflow-ext-serialization:1.1.0")
        }
    }
}
```

### Android-only extension

If you want the Android helpers (`SharedPreferenceStore` and `AndroidLogger`), add them in `androidMain`:

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation("ru.dapadz:cachedflow-ext-android:1.1.0")
        }
    }
}
```

## Getting started

### 1. Provide a `Store`

`Store` is the persistence abstraction used by CachedFlow:

```kotlin
interface Store {
    suspend fun <T : Any> get(key: StoreKey<T>): Flow<T?>
    suspend fun <T : Any> save(key: StoreKey<T>, value: T)
    suspend fun <T : Any> delete(key: StoreKey<T>)
    suspend fun clear()
}
```

For shared code, you can back it with any platform storage such as `SharedPreferences`, `DataStore`, `NSUserDefaults`, files, or your own database layer.

### 2. Initialize the cache

```kotlin
val store: Store = MyStore()
Cache.initialize(store)
```

Optionally:

```kotlin
Cache.initialize(store, logger = MyLogger())
```

### 3. Define keys

```kotlin
val userKey = stringCacheKey("user_profile")
val ageKey = integerCacheKey("user_age")
```

### 4. Cache a flow

```kotlin
flow { emit(fetchUserProfileFromApi()) }
    .cache(userKey, CacheStrategyType.IF_HAVE)
    .collect { user ->
        println("User: $user")
    }
```

## Cache strategies

| Strategy | Description |
| --- | --- |
| `IF_HAVE` | Use cache if a value already exists, otherwise execute the original flow. |
| `ONLY_REQUEST` | Always execute the original flow and optionally save the result. |
| `ONLY_CACHE` | Read from cache only. Throws when the value is missing. |

You can also implement your own strategy:

```kotlin
abstract class CacheStrategy<T>(
    protected val key: Key<T>,
    protected val cachedAfterLoad: Boolean
) {
    abstract suspend fun execute(currentFlow: Flow<T>): Flow<T>
}
```

## Built-in key factories

| Type | Factory |
| --- | --- |
| `String` | `stringCacheKey(name)` |
| `Int` | `integerCacheKey(name)` |
| `Long` | `longCacheKey(name)` |
| `Float` | `floatCacheKey(name)` |
| `Double` | `doubleCacheKey(name)` |
| `Byte` | `byteCacheKey(name)` |
| `Short` | `shortCacheKey(name)` |
| `Char` | `charCacheKey(name)` |
| `Boolean` | `booleanCacheKey(name)` |

Custom keys are supported as well:

```kotlin
class MyKey(name: String) : Key<MyType>(name) {
    override fun isTypeOf(valueClass: KClass<*>) = valueClass == MyType::class

    override suspend fun getFromStore(store: Store): Flow<MyType?> = TODO()

    override suspend fun saveToStore(item: MyType, store: Store) = TODO()
}
```

## Modules

### `cachedflow`

The multiplatform core library. Use it from shared `commonMain` code.

### `cachedflow-ext-serialization`

Adds support for `kotlinx.serialization`:

- `serializableKey`
- `serializableListKey`
- `SerializersModule` for polymorphic and advanced serialization cases

Example:

```kotlin
@Serializable
data class Dog(val name: String)

fun getGoodDog(): Flow<Dog> {
    return dogRepository.getGoodDog()
        .cache(serializableKey("goodDog"))
}
```

Polymorphic example:

```kotlin
interface Animal {
    val name: String
}

@Serializable
data class Dog(
    override val name: String,
    val isGoodBoy: Boolean
) : Animal

@Serializable
data class Cat(
    override val name: String
) : Animal

fun getAnimals(): Flow<List<Animal>> {
    return repository.getAnimals()
        .cache(
            serializableListKey(
                name = "animals",
                module = SerializersModule {
                    polymorphic(Animal::class) {
                        subclass(Cat::class)
                        subclass(Dog::class)
                    }
                }
            )
        )
}
```

### `cachedflow-ext-android`

Android-only helpers:

- `SharedPreferenceStore`
- `AndroidLogger`

Example:

```kotlin
Cache.initialize(
    store = SharedPreferenceStore(context = this),
    logger = AndroidLogger()
)
```
