# CachedFlow

**Легковесная кроссплатформенная библиотека кеширования на Kotlin**, 
разработанная для работы с системой ключей и гибкими стратегиями кеширования.

---

## Возможности

- Простой доступ к кешу по ключам с типизацией
- Настраиваемые стратегии кеширования
- Поддержка приостановленных функций и работы с Flow
- Подключаемое хранилище (`Store`) для полной гибкости на разных платформах
- Встроенный интерфейс логирования
- Полностью тестируемая и не зависящая от Android-зависимостей

---

## Установка

> Добавьте библиотеку в свой Kotlin Multiplatform проект (скоро будет доступна через Maven Central / GitHub Packages).

<details>
<summary>Gradle (Kotlin DSL)</summary>

```kotlin
dependencies {
    implementation("com.dapadz:cachedflow:<version>")
}
```

</details>

---

## Начало работы

### 1. Создайте собственную реализацию `Store`

Интерфейс `Store` абстрагирует слой хранения данных:

```kotlin
interface Store {
    suspend fun <T: Any> get(key: StoreKey<T>): Flow<T?>
    suspend fun <T: Any> save(key: StoreKey<T>, value: T)
    suspend fun <T: Any> delete(key: StoreKey<T>)
    suspend fun clear()
}
```

Реализуйте его, используя локальное хранилище вашей платформы (например, `DataStore`, `SharedPreferences`, `NSUserDefaults`, файловую систему и т.д.).

---

### 2. Инициализация кеша

```kotlin
fun main() {
    val store: Store = MyMultiplatformStore()
    Cache.initialize(store)
}
```

При необходимости можно передать собственный `Logger`:

```kotlin
Cache.initialize(store, logger = MyLogger())
```

---

### 3. Определение и использование ключей кеша

Используйте встроенные функции-хелперы для ключей или создайте свои:

```kotlin
val userKey = stringCacheKey("user_profile")
val ageKey = integerCacheKey("user_age")
```

---

### 4. Кеширование Flow с помощью стратегии

```kotlin
flow { emit(fetchUserProfileFromApi()) }
    .cache(userKey, CacheStrategyType.IF_HAVE)
    .collect { user -> println("User: $user") }
```

---

## Стратегии кеширования

Выберите, как будет вести себя кеш во время работы Flow:

| Стратегия            | Описание                                                   |
|-----------------------|-----------------------------------------------------------|
| `ONLY_CACHE`          | Всегда использовать кеш. Бросает ошибку, если значения нет.|
| `ONLY_REQUEST`        | Пропустить кеш. Всегда получать новые данные и опционально кешировать.|
| `IF_HAVE` *(по умолчанию)* | Использовать кеш, если он есть, иначе выполнить Flow.   |

#### Создание своей стратегии CacheStrategy

Реализуйте интерфейс `CacheStrategy<T>` для полного контроля:

```kotlin
abstract class CacheStrategy <T> (
    protected val key: Key<T>,
    protected val cachedAfterLoad : Boolean
) {
    abstract suspend fun execute(currentFlow: Flow<T>): Flow<T>
}
```

---

## Ключи

Используйте следующие функции-фабрики для быстрого определения ключей для примитивных типов:

| Тип ключа | Функция-фабрика            | Пример                                   |
|-----------|-----------------------------|------------------------------------------|
| `String`  | `stringCacheKey(name)`      | `val key = stringCacheKey("username")`   |
| `Int`     | `integerCacheKey(name)`     | `val key = integerCacheKey("user_age")`  |
| `Long`    | `longCacheKey(name)`        | `val key = longCacheKey("view_count")`   |
| `Float`   | `floatCacheKey(name)`       | `val key = floatCacheKey("user_score")`  |
| `Double`  | `doubleCacheKey(name)`      | `val key = doubleCacheKey("price")`      |
| `Byte`    | `byteCacheKey(name)`        | `val key = byteCacheKey("retry_count")`  |
| `Short`   | `shortCacheKey(name)`       | `val key = shortCacheKey("port")`        |
| `Char`    | `charCacheKey(name)`        | `val key = charCacheKey("grade")`        |
| `Boolean` | `booleanCacheKey(name)`     | `val key = booleanCacheKey("is_logged")` |

Эти ключи наследуются от `Key<T>` и включают встроенную логику для безопасных по типам операций кеширования.

#### Пример собственного ключа

Вы также можете определить собственный ключ для сложных или кастомных типов:

```kotlin
class MyKey(name: String): Key<MyType>(name) {
    override fun isTypeOf(valueClass: KClass<*>) = valueClass == MyType::class
    override suspend fun getFromStore(store: Store): Flow<MyType?> = ...
    override suspend fun saveToStore(item: MyType, store: Store) = ...
}
```

# Расширения

Для упрощения интеграции и расширения возможностей доступны дополнительные модули.  
Они позволяют быстрее подключить библиотеку под конкретные платформы и сценарии использования.

## Android

Набор расширений для удобной работы на Android.  
Модуль включает:
- готовую реализацию `Store` на основе `SharedPreferences`
- логгер `AndroidLogger`, использующий стандартный `Log`

### Установка

```kotlin
dependencies {
    implementation("com.dapadz:cachedflow:<version>")
    implementation("com.dapadz:cachedflow-ext-android:<version>")
}
```

### Использование

Пример инициализации `Cache` с использованием `SharedPreferenceStore` и `AndroidLogger`:

```kotlin
private fun initializeCache() {
   Cache.initialize(
       store = SharedPreferenceStore(context = this),
       logger = AndroidLogger()
   )
}
```

## Kotlin Serialization

Расширение, добавляющее поддержку [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization).  
Позволяет сохранять и восстанавливать `Serializable` классы из кеша.

Включает удобные ключи:
- `serializableKey` — для одного объекта
- `serializableListKey` — для списка объектов

### Установка

```kotlin
dependencies {
    implementation("com.dapadz:cachedflow:<version>")
    implementation("com.dapadz:cachedflow-ext-serialization:<version>")
}
```

### Использование

Пример кеширования `Serializable` класса:

```kotlin
@Serializable
data class Dog(val name: String)

fun getGoodDog(): Flow<Dog> {
    return dogRepository.getGoodDog()
        .cache(serializableKey("goodDog"))
}
```

Также можно использовать `SerializersModule` для более сложных сценариев —  
например, сериализации интерфейсов и полиморфных классов:

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
