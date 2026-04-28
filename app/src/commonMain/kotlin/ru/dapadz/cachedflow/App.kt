package ru.dapadz.cachedflow

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.dapadz.cachedflow.cache.strategy.CacheStrategyType

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun App(environment: DemoEnvironment) {
    val runner = remember(environment.platformName, environment.storeLabel) {
        DemoScenarioRunner(environment)
    }
    val uiState by runner.uiState.collectAsState()
    val logs by runner.logs.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val primitiveActions = remember(runner) { runner.primitiveActions() }
    val serializationActions = remember(runner) { runner.serializationActions() }

    var busy by remember { mutableStateOf(false) }
    var cachedAfterLoad by remember { mutableStateOf(true) }

    fun launchAction(action: suspend () -> Unit) {
        if (busy) return
        scope.launch {
            busy = true
            try {
                action()
            } catch (throwable: Throwable) {
                snackbarHostState.showSnackbar(
                    throwable.message ?: throwable::class.simpleName ?: "Unknown error"
                )
            } finally {
                busy = false
            }
        }
    }

    MaterialTheme(
        colorScheme = demoColorScheme()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                Color(0xFFF4F0E6),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .safeDrawingPadding()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(28.dp),
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "CachedFlow Multiplatform Demo",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PlatformPill(text = environment.platformName)
                                PlatformPill(text = environment.storeLabel)
                            }
                            Text(
                                text = DemoScreenText.overview(
                                    platformName = environment.platformName,
                                    storeLabel = environment.storeLabel
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    DemoSection(
                        title = "General actions",
                        subtitle = "Cache lifecycle and demo log controls."
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { launchAction { runner.clearCache() } },
                                enabled = !busy
                            ) {
                                Text("Clear cache")
                            }
                            OutlinedButton(
                                onClick = runner::clearLog,
                                enabled = !busy
                            ) {
                                Text("Clear log")
                            }
                        }
                    }

                    DemoSection(
                        title = "Strategies",
                        subtitle = "Compare ONLY_REQUEST, IF_HAVE, and ONLY_CACHE from shared code."
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "cachedAfterLoad",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = cachedAfterLoad,
                                onCheckedChange = { cachedAfterLoad = it },
                                enabled = !busy
                            )
                        }
                        HorizontalDivider()
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StrategyButton(
                                title = "ONLY_REQUEST",
                                enabled = !busy
                            ) {
                                launchAction {
                                    runner.runStrategy(CacheStrategyType.ONLY_REQUEST, cachedAfterLoad)
                                }
                            }
                            StrategyButton(
                                title = "IF_HAVE",
                                enabled = !busy
                            ) {
                                launchAction {
                                    runner.runStrategy(CacheStrategyType.IF_HAVE, cachedAfterLoad)
                                }
                            }
                            StrategyButton(
                                title = "ONLY_CACHE",
                                enabled = !busy
                            ) {
                                launchAction {
                                    runner.runStrategy(CacheStrategyType.ONLY_CACHE, cachedAfterLoad)
                                }
                            }
                        }
                        HorizontalDivider()
                        Text(
                            text = uiState.strategyStatus,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    DemoActionSection(
                        title = "Primitive keys",
                        subtitle = "Every built-in primitive adapter round-trips through the cache.",
                        actions = primitiveActions,
                        busy = busy,
                        onAction = ::launchAction
                    )

                    DemoActionSection(
                        title = "Serialization extension",
                        subtitle = "Shared scenarios for serializable objects, lists, polymorphism, and broken JSON.",
                        actions = serializationActions,
                        busy = busy,
                        onAction = ::launchAction
                    )

                    DemoSection(
                        title = "Cache dump",
                        subtitle = "Raw contents of the backing store after the latest action."
                    ) {
                        SelectionContainer {
                            Text(
                                text = uiState.cacheDump,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    DemoSection(
                        title = "Event log",
                        subtitle = "Combined demo events and internal CachedFlow logger output."
                    ) {
                        SelectionContainer {
                            Text(
                                text = if (logs.isEmpty()) DemoScreenText.emptyLog else logs.joinToString("\n"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DemoSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                content()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DemoActionSection(
    title: String,
    subtitle: String,
    actions: List<DemoActionSpec>,
    busy: Boolean,
    onAction: (suspend () -> Unit) -> Unit
) {
    DemoSection(title = title, subtitle = subtitle) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            actions.forEach { action ->
                when (action.style) {
                    DemoButtonStyle.Filled -> Button(
                        onClick = { onAction(action.run) },
                        enabled = !busy
                    ) {
                        Text(action.title)
                    }

                    DemoButtonStyle.Outlined -> OutlinedButton(
                        onClick = { onAction(action.run) },
                        enabled = !busy
                    ) {
                        Text(action.title)
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyButton(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text(title)
    }
}

private fun demoColorScheme() = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF7B341E),
    onPrimary = Color(0xFFFFF7F2),
    primaryContainer = Color(0xFFF0C7A6),
    onPrimaryContainer = Color(0xFF2E130A),
    secondary = Color(0xFF3C6E71),
    onSecondary = Color(0xFFF3FBFB),
    secondaryContainer = Color(0xFFCFE5E5),
    onSecondaryContainer = Color(0xFF132728),
    background = Color(0xFFFFFBF5),
    onBackground = Color(0xFF201A18),
    surface = Color(0xFFFFF7F0),
    onSurface = Color(0xFF201A18),
    surfaceVariant = Color(0xFFF2E2D4),
    onSurfaceVariant = Color(0xFF5B5148)
)
