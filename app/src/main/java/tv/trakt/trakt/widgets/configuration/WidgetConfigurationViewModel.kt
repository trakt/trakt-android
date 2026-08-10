package tv.trakt.trakt.widgets.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.widgets.calendar.CalendarWidgetUpdater
import tv.trakt.trakt.widgets.continuewatching.ContinueWatchingWidgetUpdater
import tv.trakt.trakt.widgets.data.WidgetAppearanceStore
import tv.trakt.trakt.widgets.model.WidgetBackground
import tv.trakt.trakt.widgets.streaks.StreaksWidgetUpdater

@Suppress("UNCHECKED_CAST")
internal class WidgetConfigurationViewModel(
    private val appWidgetId: Int,
    private val appearanceStore: WidgetAppearanceStore,
    private val continueWatchingUpdater: ContinueWatchingWidgetUpdater,
    private val calendarUpdater: CalendarWidgetUpdater,
    private val streaksUpdater: StreaksWidgetUpdater,
) : ViewModel() {
    private val initialState = WidgetConfigurationState()

    private val backgroundState = MutableStateFlow(initialState.background)
    private val titleVisibleState = MutableStateFlow(initialState.titleVisible)
    private val loadingState = MutableStateFlow(initialState.loading)

    init {
        loadAppearance()
    }

    private fun loadAppearance() {
        viewModelScope.launch {
            try {
                val appearance = appearanceStore.get(appWidgetId)
                backgroundState.update { appearance.background }
                titleVisibleState.update { appearance.titleVisible }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Failed to read the widget appearance")
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    /** Applied on tap rather than on save: the widget behind the sheet is the real preview. */
    fun setBackground(background: WidgetBackground) {
        backgroundState.update { background }

        persist { appearanceStore.setBackground(appWidgetId = appWidgetId, background = background) }
    }

    fun setTitleVisible(visible: Boolean) {
        titleVisibleState.update { visible }

        persist { appearanceStore.setTitleVisible(appWidgetId = appWidgetId, visible = visible) }
    }

    private fun persist(write: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                write()
                // Each updater renders only the ids its widget owns, so all can run blindly.
                continueWatchingUpdater.render(appWidgetId = appWidgetId)
                calendarUpdater.render(appWidgetId = appWidgetId)
                streaksUpdater.render(appWidgetId = appWidgetId)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Failed to store the widget appearance")
                }
            }
        }
    }

    // Explicit element type: the three sources share no useful supertype to infer.
    val state = combine<Any, WidgetConfigurationState>(
        backgroundState,
        titleVisibleState,
        loadingState,
    ) { state ->
        WidgetConfigurationState(
            background = state[0] as WidgetBackground,
            titleVisible = state[1] as Boolean,
            loading = state[2] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
