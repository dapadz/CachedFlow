package ru.dapadz.cachedflow

internal data class DemoActionSpec(
    val title: String,
    val style: DemoButtonStyle = DemoButtonStyle.Outlined,
    val run: suspend () -> Unit
)

internal enum class DemoButtonStyle {
    Filled,
    Outlined
}
