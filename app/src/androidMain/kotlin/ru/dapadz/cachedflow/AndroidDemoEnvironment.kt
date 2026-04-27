package ru.dapadz.cachedflow

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.edit
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.flow.Flow
import ru.dapadz.cachedflow.cache.android.AndroidLogger
import ru.dapadz.cachedflow.cache.android.SharedPreferenceStore
import ru.dapadz.cachedflow.store.StoreKey

internal fun createAndroidDemoEnvironment(context: Context): DemoEnvironment {
    val appContext = context.applicationContext
    return DemoEnvironment(
        platformName = "Android",
        storeLabel = "ext:android SharedPreferenceStore",
        store = AndroidDemoStore(appContext),
        logger = AndroidLogger()
    )
}

internal class AndroidDemoStore(
    context: Context
) : DemoInspectableStore {

    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE)
    private val delegate = SharedPreferenceStore(appContext)

    override suspend fun clear() {
        delegate.clear()
    }

    override suspend fun <T : Any> delete(key: StoreKey<T>) {
        delegate.delete(key)
    }

    override suspend fun <T : Any> get(key: StoreKey<T>): Flow<T?> {
        return delegate.get(key)
    }

    override suspend fun <T : Any> save(key: StoreKey<T>, value: T) {
        delegate.save(key, value)
    }

    override suspend fun putRawString(name: String, value: String) {
        preferences.edit {
            putString(name, value)
        }
    }

    override fun dumpEntries(): Map<String, Any?> {
        return preferences.all.toSortedMap()
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val environment = createAndroidDemoEnvironment(applicationContext)
        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                App(environment)
            }
        }

        setContentView(composeView)
    }
}
