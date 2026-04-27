# ⚠️ **Disclaimer**  ⚠️
> This is a **pre-release version**. It is not yet ready for general use and is currently being tested in internal projects.  
> Use it in your own projects **at your own risk**.

# CachedFlow

**A lightweight Kotlin Multiplatform caching library** designed to work using a key-based strategy system and flexible caching policies.

---

## Features

- Simple key-based cache access using typed keys
- Customizable caching strategies
- Supports suspending and flow-based data operations
- Pluggable storage backend (`Store`) for full platform flexibility
- Built-in logging interface
- Fully testable and decoupled from Android dependencies

---

## Installation

> Add the library to your Kotlin Multiplatform project (available soon via Maven Central / GitHub Packages).

<details>
<summary>Gradle (Kotlin DSL)</summary>

```kotlin
dependencies {
    implementation("ru.dapadz:cachedflow:1.0.0")
}
```

</details>

---

## Getting Started

### 1. Create your own `Store` implementation

The `Store` interface abstracts the storage layer:

```kotlin
interface Store {
    suspend fun <T: Any> get(key: StoreKey<T>): Flow<T?>
    suspend fun <T: Any> save(key: StoreKey<T>, value: T)
    suspend fun <T: Any> delete(key: StoreKey<T>)
    suspend fun clear()
}
```

Implement it using your platform’s local storage (e.g. `DataStore`, `SharedPreferences`, `NSUserDefaults`, file system, etc).

---

### 2. Initialize the Cache

```kotlin
fun main() {
    val store: Store = MyMultiplatformStore()
    Cache.initialize(store)
}
```

Optionally, provide a custom `Logger`:

```kotlin
Cache.initialize(store, logger = MyLogger())
```

---

### 3. Define and Use Cache Keys

Use built-in key helpers or define your own:

```kotlin
val userKey = stringCacheKey("user_profile")
val ageKey = integerCacheKey("user_age")
```

---

### 4. Cache a Flow with Strategy

```kotlin
flow { emit(fetchUserProfileFromApi()) }
    .cache(userKey, CacheStrategyType.IF_HAVE)
    .collect { user -> println("User: $user") }
```

---

## Cache Strategies

Choose how the cache behaves during a Flow emission:

| Strategy            | Description                                                  |
|---------------------|--------------------------------------------------------------|
| `ONLY_CACHE`        | Always use the cache. Throws if no value exists.             |
| `ONLY_REQUEST`      | Always skip cache. Fetch fresh and optionally cache it.      |
| `IF_HAVE` *(default)* | Use cache if available, otherwise fallback to the flow.     |

#### Extend with Your Own CacheStrategy

Implement the `CacheStrategy<T>` interface for full control:

```kotlin
abstract class CacheStrategy <T> (
    protected val key: Key<T>,
    protected val cachedAfterLoad : Boolean
) {
    abstract suspend fun execute(currentFlow: Flow<T>): Flow<T>
}
```

---

## Keys

Use the following factory functions to quickly define typed keys for common primitive types:

| Key Type | Factory Function         | Example                                  |
|----------|--------------------------|------------------------------------------|
| `String` | `stringCacheKey(name)`   | `val key = stringCacheKey("username")`   |
| `Int`    | `integerCacheKey(name)`  | `val key = integerCacheKey("user_age")`  |
| `Long`   | `longCacheKey(name)`     | `val key = longCacheKey("view_count")`   |
| `Float`  | `floatCacheKey(name)`    | `val key = floatCacheKey("user_score")`  |
| `Double` | `doubleCacheKey(name)`   | `val key = doubleCacheKey("price")`      |
| `Byte`   | `byteCacheKey(name)`     | `val key = byteCacheKey("retry_count")`  |
| `Short`  | `shortCacheKey(name)`    | `val key = shortCacheKey("port")`        |
| `Char`   | `charCacheKey(name)`     | `val key = charCacheKey("grade")`        |
| `Boolean`| `booleanCacheKey(name)`  | `val key = booleanCacheKey("is_logged")` |

These keys inherit from `Key<T>` and include built-in logic to handle type-safe caching operations.

#### Custom Key Example

You can also define your own cache key for complex or custom types:

```kotlin
class MyKey(name: String): Key<MyType>(name) {
    override fun isTypeOf(valueClass: KClass<*>) = valueClass == MyType::class
    override suspend fun getFromStore(store: Store): Flow<MyType?> = ...
    override suspend fun saveToStore(item: MyType, store: Store) = ...
}
```

# Extension

To simplify integration and expand functionality, additional extension modules are available.
They allow you to quickly connect the library for specific platforms and use cases.

## Android

A set of extensions for convenient work on Android.
The module includes:
- a ready-to-use `Store` implementation based on `SharedPreferences`
- `AndroidLogger`, which uses the standard `Log`

### Installation

```kotlin
dependencies {
    implementation("ru.dapadz:cachedflow:1.0.0")
    implementation("ru.dapadz:cachedflow-ext-android:1.0.0")
}
```

### Usage

Example of initializing `Cache` with `SharedPreferenceStore` and `AndroidLogger`:

```kotlin
private fun initializeCache() {
   Cache.initialize(
       store = SharedPreferenceStore(context = this),
       logger = AndroidLogger()
   )
}
```

## Kotlin Serialization

An extension that adds support for [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization).
It allows storing and restoring `Serializable` classes from the cache.

Includes convenient keys:
- `serializableKey` — for a single object
- `serializableListKey` — for a list of objects

### Installation

```kotlin
dependencies {
    implementation("com.dapadz:cachedflow:<version>")
    implementation("com.dapadz:cachedflow-ext-serialization:<version>")
}
```

### Usage

Example of caching a `Serializable` class:

```kotlin
@Serializable
data class Dog(val name: String)

fun getGoodDog(): Flow<Dog> {
    return dogRepository.getGoodDog()
        .cache(serializableKey("goodDog"))
}
```

You can also use `SerializersModule` for more complex scenarios —
for example, serializing interfaces and polymorphic classes:

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
