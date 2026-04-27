package ru.dapadz.cachedflow

import android.app.Application
import ru.dapadz.cachedflow.cache.Cache
import ru.dapadz.cachedflow.cache.android.SharedPreferenceStore

class DemoCacheApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Cache.initialize(
            store = SharedPreferenceStore(applicationContext),
            logger = DemoLogger()
        )
        DemoLogStore.add("App", "Cache initialized with SharedPreferenceStore and AndroidLogger")
    }
}
