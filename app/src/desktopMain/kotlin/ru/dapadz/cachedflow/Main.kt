package ru.dapadz.cachedflow

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

private fun createDesktopDemoEnvironment(): DemoEnvironment {
    return DemoEnvironment(
        platformName = "Desktop",
        storeLabel = "In-memory demo store",
        store = InMemoryDemoStore()
    )
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CachedFlow Multiplatform Demo"
    ) {
        val environment = remember { createDesktopDemoEnvironment() }
        App(environment)
    }
}
