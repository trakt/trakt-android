package tv.trakt.trakt.core.summary.shows.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.SEASON
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.summary.shows.features.history.usecases.GetShowHistoryUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

@OptIn(FlowPreview::class)
internal class ShowHistoryViewModel(
    private val show: Show,
    private val getHistoryUseCase: GetShowHistoryUseCase,
    private val showDetailsUpdates: ShowDetailsUpdates,
    private val episodeDetailsUpdates: EpisodeDetailsUpdates,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = ShowHistoryState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.SHOW_HISTORY))

    private var collapseJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
        observeLists()
    }

    private fun observeLists() {
        merge(
            showDetailsUpdates.observeUpdates(Source.PROGRESS),
            showDetailsUpdates.observeUpdates(Source.SEASONS),
            episodeDetailsUpdates.observeUpdates(PROGRESS),
            episodeDetailsUpdates.observeUpdates(SEASON),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

    fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            try {
                if (itemsState.value == initialState.items) {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getHistoryUseCase.getHistory(show.ids.trakt)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }
        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.SHOW_HISTORY)
                else -> collapsingManager.expand(CollapsingKey.SHOW_HISTORY)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowHistoryState> = combine(
        itemsState,
        loadingState,
        errorState,
        collapseState,
    ) { state ->
        ShowHistoryState(
            items = state[0] as ImmutableList<HomeActivityItem.EpisodeItem>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
            collapsed = state[3] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
