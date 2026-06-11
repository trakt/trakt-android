package tv.trakt.trakt.helpers.editscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.helpers.editscreen.data.EditScreenManager
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey

internal class EditScreenViewModel(
    enabledValues: Set<EditScreenKey>,
    private val editScreenManager: EditScreenManager,
) : ViewModel() {
    val initialState = EditScreenState(
        values = enabledValues
            .associateWith { editScreenManager.isVisible(setOf(it)) }
            .toImmutableMap(),
    )

    private val valuesState = MutableStateFlow(initialState.values)

    init {
        editScreenManager.observe(enabledValues)
            .onEach { values ->
                valuesState.update {
                    values.toImmutableMap()
                }
            }.launchIn(viewModelScope)
    }

    fun toggle(key: EditScreenKey) {
        val currentValues = valuesState.value ?: return
        val isVisible = currentValues[key] ?: return

        viewModelScope.launch {
            if (isVisible) {
                // Only hide if there's more than 1 visible, to prevent hiding all sections.
                val visibleCount = currentValues.values.count { it }
                if (visibleCount > 1) {
                    editScreenManager.hide(key)
                }
            } else {
                editScreenManager.show(key)
            }
        }
    }

    val state = combine(
        valuesState,
    ) { state ->
        EditScreenState(
            values = state[0],
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditScreenState(),
    )
}
