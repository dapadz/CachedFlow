package ru.dapadz.cachedflow

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

internal fun createIosDemoEnvironment(): DemoEnvironment {
    return DemoEnvironment(
        platformName = "iOS",
        storeLabel = "In-memory demo store",
        store = InMemoryDemoStore()
    )
}

@Suppress("FunctionName")
fun MainViewController(): UIViewController = ComposeUIViewController {
    val environment = remember { createIosDemoEnvironment() }
    App(environment)
}
