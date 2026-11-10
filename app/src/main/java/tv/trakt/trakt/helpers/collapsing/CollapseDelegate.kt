package tv.trakt.trakt.helpers.collapsing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class CollapseDelegate(
    private val manager: CollapsingManager,
    private val scope: CoroutineScope,
    private val key: () -> CollapsingKey,
) {
    constructor(
        manager: CollapsingManager,
        scope: CoroutineScope,
        key: CollapsingKey,
    ) : this(
        manager = manager,
        scope = scope,
        key = { key },
    )

    private val collapsedState = MutableStateFlow(manager.isCollapsed(key()))
    private var job: Job? = null

    val state = collapsedState.asStateFlow()

    fun set(collapsed: Boolean) {
        collapsedState.update { collapsed }

        job?.cancel()
        job = scope.launch {
            val currentKey = key()
            when {
                collapsed -> manager.collapse(currentKey)
                else -> manager.expand(currentKey)
            }
        }
    }

    fun refresh() {
        collapsedState.update { manager.isCollapsed(key()) }
    }
}
