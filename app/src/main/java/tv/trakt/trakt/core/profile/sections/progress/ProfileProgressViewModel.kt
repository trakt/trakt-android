package tv.trakt.trakt.core.profile.sections.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.lists.ListsConfig.PROGRESS_SECTION_LIMIT
import tv.trakt.trakt.core.profile.sections.progress.filters.GetProgressFilterUseCase
import tv.trakt.trakt.core.profile.sections.progress.model.ProfileProgressItem
import tv.trakt.trakt.core.profile.sections.progress.model.ProgressFilter
import tv.trakt.trakt.core.profile.sections.progress.model.ProgressFilter.Completed
import tv.trakt.trakt.core.profile.sections.progress.model.ProgressFilter.Dropped
import tv.trakt.trakt.core.profile.sections.progress.model.ProgressFilter.InProgress
import tv.trakt.trakt.core.profile.sections.progress.usecase.GetProgressCompleteUseCase
import tv.trakt.trakt.core.profile.sections.progress.usecase.GetProgressDroppedUseCase
import tv.trakt.trakt.core.profile.sections.progress.usecase.GetProgressWatchingUseCase
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.HISTORY
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.SEASON
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import kotlin.time.Duration.Companion.milliseconds

internal class ProfileProgressViewModel(
    private val getFilterUseCase: GetProgressFilterUseCase,
    private val getDroppedUseCase: GetProgressDroppedUseCase,
    private val getCompletedUseCase: GetProgressCompleteUseCase,
    private val getWatchingUseCase: GetProgressWatchingUseCase,
    private val localShowSource: ShowLocalDataSource,
    private val collapsingManager: CollapsingManager,
    private val showUpdates: ShowDetailsUpdates,
    private val episodeUpdates: EpisodeDetailsUpdates,
) : ViewModel() {
    private val initialState = ProfileProgressState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val collapseState = MutableStateFlow(isCollapsed())
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()
        observeData()
    }

    @OptIn(FlowPreview::class)
    private fun observeData() {
        merge(
            showUpdates.observeUpdates(Source.Progress),
            showUpdates.observeUpdates(Source.Seasons),
            showUpdates.observeUpdates(Source.WatchedUntil),
            episodeUpdates.observeUpdates(PROGRESS),
            episodeUpdates.observeUpdates(SEASON),
            episodeUpdates.observeUpdates(HISTORY),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    fun loadData(ignoreErrors: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                loadingState.update { Idle }
                errorState.update { null }

                val filter = loadFilter()

                itemsState.update {
                    when (filter) {
                        Completed -> getCompletedUseCase.getLocalCompleted(PROGRESS_SECTION_LIMIT)
                        InProgress -> getWatchingUseCase.getLocalWatching(PROGRESS_SECTION_LIMIT)
                        Dropped -> getDroppedUseCase.getLocalDropped(PROGRESS_SECTION_LIMIT)
                    }
                }

                loadingState.update {
                    when {
                        itemsState.value.isNullOrEmpty() -> Loading
                        else -> Done
                    }
                }

                itemsState.update {
                    when (filter) {
                        Completed -> getCompletedUseCase.getCompleted(page = 1, limit = PROGRESS_SECTION_LIMIT)
                        InProgress -> getWatchingUseCase.getWatching(page = 1, limit = PROGRESS_SECTION_LIMIT)
                        Dropped -> getDroppedUseCase.getDropped(page = 1, limit = PROGRESS_SECTION_LIMIT)
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                loadDataJob = null
            }
        }
    }

    private suspend fun loadFilter(): ProgressFilter {
        val filter = getFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
    }

    fun setFilter(newFilter: ProgressFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getFilterUseCase.setFilter(newFilter)
            loadData()
        }
    }

    fun navigateToShow(show: Show) {
        viewModelScope.launch {
            localShowSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.PROFILE_PROGRESS)
                else -> collapsingManager.expand(CollapsingKey.PROFILE_PROGRESS)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(CollapsingKey.PROFILE_PROGRESS)
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        itemsState,
        filterState,
        navigateShow,
        collapseState,
        errorState,
        userState,
    ) { states ->
        ProfileProgressState(
            loading = states[0] as LoadingState,
            items = states[1] as ImmutableList<ProfileProgressItem>?,
            filter = states[2] as ProgressFilter?,
            navigateShow = states[3] as TraktId?,
            collapsed = states[4] as Boolean,
            error = states[5] as Exception?,
            user = states[6] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
