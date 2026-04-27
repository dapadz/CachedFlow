package ru.dapadz.cachedflow

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.dapadz.cachedflow.cache.strategy.CacheStrategyType
import ru.dapadz.cachedflow.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var scenarioRunner: DemoScenarioRunner

    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val interactiveViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scenarioRunner = DemoScenarioRunner(applicationContext)

        renderStaticContent()
        bindCommonActions()
        bindStrategyActions()
        populateGrid(binding.primitiveActionsGrid, scenarioRunner.primitiveActions())
        populateGrid(binding.serializationActionsGrid, scenarioRunner.serializationActions())
        observeUiState()
        observeLogs()
    }

    override fun onDestroy() {
        uiScope.cancel()
        super.onDestroy()
    }

    private fun renderStaticContent() {
        binding.overviewText.text = DemoScreenText.overview
        binding.eventLogText.text = DemoScreenText.emptyLog
    }

    private fun bindCommonActions() {
        bindButton(binding.clearCacheButton) {
            scenarioRunner.clearCache()
        }

        registerInteractiveView(binding.clearLogButton)
        binding.clearLogButton.setOnClickListener {
            scenarioRunner.clearLog()
        }
    }

    private fun bindStrategyActions() {
        registerInteractiveView(binding.cacheAfterLoadCheckbox)

        bindButton(binding.onlyRequestButton) {
            scenarioRunner.runStrategy(
                strategy = CacheStrategyType.ONLY_REQUEST,
                cachedAfterLoad = binding.cacheAfterLoadCheckbox.isChecked
            )
        }

        bindButton(binding.ifHaveButton) {
            scenarioRunner.runStrategy(
                strategy = CacheStrategyType.IF_HAVE,
                cachedAfterLoad = binding.cacheAfterLoadCheckbox.isChecked
            )
        }

        bindButton(binding.onlyCacheButton) {
            scenarioRunner.runStrategy(
                strategy = CacheStrategyType.ONLY_CACHE,
                cachedAfterLoad = binding.cacheAfterLoadCheckbox.isChecked
            )
        }
    }

    private fun observeUiState() {
        uiScope.launch {
            scenarioRunner.uiState.collect { state ->
                binding.strategyStatusText.text = state.strategyStatus
                binding.cacheDumpText.text = state.cacheDump
            }
        }
    }

    private fun observeLogs() {
        uiScope.launch {
            DemoLogStore.entries.collect { entries ->
                binding.eventLogText.text = if (entries.isEmpty()) {
                    DemoScreenText.emptyLog
                } else {
                    entries.joinToString(separator = "\n")
                }
            }
        }
    }

    private fun populateGrid(
        container: GridLayout,
        actions: List<DemoActionSpec>
    ) {
        actions.forEach { action ->
            container.addView(createGridButton(action))
        }
    }

    private fun createGridButton(action: DemoActionSpec): MaterialButton {
        return MaterialButton(this, null, action.style.materialStyleAttr).apply {
            text = action.title
            isAllCaps = false
            insetTop = 0
            insetBottom = 0
            minimumHeight = dp(52)
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }
            bindButton(this, action.run)
        }
    }

    private fun bindButton(
        button: MaterialButton,
        action: suspend () -> Unit
    ) {
        registerInteractiveView(button)
        button.setOnClickListener {
            launchAction(action)
        }
    }

    private fun launchAction(action: suspend () -> Unit) {
        uiScope.launch {
            setBusy(true)
            try {
                action()
            } catch (throwable: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    throwable.message ?: throwable::class.java.simpleName,
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setBusy(false)
            }
        }
    }

    private fun registerInteractiveView(view: View) {
        interactiveViews += view
    }

    private fun setBusy(isBusy: Boolean) {
        interactiveViews.forEach { view ->
            view.isEnabled = !isBusy
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

private val DemoButtonStyle.materialStyleAttr: Int
    get() = when (this) {
        DemoButtonStyle.Filled -> com.google.android.material.R.attr.materialButtonStyle
        DemoButtonStyle.Outlined -> com.google.android.material.R.attr.materialButtonOutlinedStyle
    }
