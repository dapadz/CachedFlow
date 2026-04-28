# CachedFlow

**Легковесная библиотека кеширования для Kotlin Flow с полноценной поддержкой Kotlin Multiplatform.**

Начиная с версии **`1.1.0`**, CachedFlow можно использовать напрямую из `commonMain` в проектах Kotlin Multiplatform и Compose Multiplatform.

## Поддерживаемые таргеты

| Модуль | Android | Desktop JVM | iOS |
| --- | --- | --- | --- |
| `cachedflow` | Да | Да | Да |
| `cachedflow-ext-serialization` | Да | Да | Да |
| `cachedflow-ext-android` | Да | Нет | Нет |

В репозитории также есть demo-приложение, которое проверяет библиотеку на **Android, Desktop и iOS**.

## Возможности

- Типизированные ключи кеша для примитивных и кастомных типов
- Стратегии работы с кешем для Flow: `IF_HAVE`, `ONLY_REQUEST`, `ONLY_CACHE`
- Абстракция `Store` для подключения платформенного хранилища
- Опциональное логирование через `Logger`
- Multiplatform-ready API для общей бизнес-логики
- Расширение на базе `kotlinx.serialization` для объектов и списков

## Подключение

### Kotlin Multiplatform / Compose Multiplatform

Подключение в `commonMain`:

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

### Android-расширение

Если нужны Android-хелперы (`SharedPreferenceStore` и `AndroidLogger`), добавьте их в `androidMain`:

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation("ru.dapadz:cachedflow-ext-android:1.1.0")
        }
    }
}
```

## Быстрый старт

### 1. Реализуйте `Store`

`Store` — это абстракция слоя хранения данных:

```kotlin
interface Store {
    suspend fun <T : Any> get(key: StoreKey<T>): Flow<T?>
    suspend fun <T : Any> save(key: StoreKey<T>, value: T)
    suspend fun <T : Any> delete(key: StoreKey<T>)
    suspend fun clear()
}
```

В shared-коде его можно реализовать поверх любого платформенного хранилища: `SharedPreferences`, `DataStore`, `NSUserDefaults`, файловой системы или собственной БД.

### 2. Инициализируйте кеш

```kotlin
val store: Store = MyStore()
Cache.initialize(store)
```

При необходимости:

```kotlin
Cache.initialize(store, logger = MyLogger())
```

### 3. Определите ключи

```kotlin
val userKey = stringCacheKey("user_profile")
val ageKey = integerCacheKey("user_age")
```

### 4. Закешируйте Flow

```kotlin
flow { emit(fetchUserProfileFromApi()) }
    .cache(userKey, CacheStrategyType.IF_HAVE)
    .collect { user ->
        println("User: $user")
    }
```

## Стратегии кеширования

| Стратегия | Описание |
| --- | --- |
| `IF_HAVE` | Использовать кеш, если значение уже есть, иначе выполнить исходный Flow. |
| `ONLY_REQUEST` | Всегда выполнять исходный Flow и при необходимости сохранять результат. |
| `ONLY_CACHE` | Читать только из кеша. Если значения нет, будет ошибка. |

Можно реализовать и собственную стратегию:

```kotlin
abstract class CacheStrategy<T>(
    protected val key: Key<T>,
    protected val cachedAfterLoad: Boolean
) {
    abstract suspend fun execute(currentFlow: Flow<T>): Flow<T>
}
```

## Встроенные фабрики ключей

| Тип | Фабрика |
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

Можно использовать и собственные ключи:

```kotlin
class MyKey(name: String) : Key<MyType>(name) {
    override fun isTypeOf(valueClass: KClass<*>) = valueClass == MyType::class

    override suspend fun getFromStore(store: Store): Flow<MyType?> = TODO()

    override suspend fun saveToStore(item: MyType, store: Store) = TODO()
}
```

## Модули

### `cachedflow`

Базовый multiplatform-модуль. Используется из общего `commonMain` кода.

### `cachedflow-ext-serialization`

Расширение с поддержкой `kotlinx.serialization`:

- `serializableKey`
- `serializableListKey`
- `SerializersModule` для полиморфной и расширенной сериализации

Пример:

```kotlin
@Serializable
data class Dog(val name: String)

fun getGoodDog(): Flow<Dog> {
    return dogRepository.getGoodDog()
        .cache(serializableKey("goodDog"))
}
```

Пример с полиморфизмом:

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

Пример:

```kotlin
Cache.initialize(
    store = SharedPreferenceStore(context = this),
    logger = AndroidLogger()
)
```
