@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.profile.sections.progress.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
import tv.trakt.trakt.core.lists.ListsConfig.PROGRESS_PAGE_LIMIT
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
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.SEASON
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source

internal class AllProgressViewModel(
    private val getFilterUseCase: GetProgressFilterUseCase,
    private val getCompletedUseCase: GetProgressCompleteUseCase,
    private val getDroppedUseCase: GetProgressDroppedUseCase,
    private val getWatchingUseCase: GetProgressWatchingUseCase,
    private val localShowSource: ShowLocalDataSource,
    private val showUpdates: ShowDetailsUpdates,
    private val episodeUpdates: EpisodeDetailsUpdates,
) : ViewModel() {
    private val initialState = AllProgressState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null

    private var page: Int = 1
    private var hasMoreData: Boolean = false

    init {
        loadData()
        observeData()
    }

    @OptIn(FlowPreview::class)
    private fun observeData() {
        merge(
            showUpdates.observeUpdates(Source.PROGRESS),
            showUpdates.observeUpdates(Source.SEASONS),
            episodeUpdates.observeUpdates(PROGRESS),
            episodeUpdates.observeUpdates(SEASON),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    fun loadData(ignoreErrors: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                page = 1

                loadingState.update { Idle }
                errorState.update { null }

                if (filterState.value == null) {
                    filterState.update {
                        getFilterUseCase.getFilter()
                    }
                }

                itemsState.update {
                    fetchLocalData(
                        filter = filterState.value ?: Completed,
                    )
                }

                loadingState.update {
                    when {
                        itemsState.value.isNullOrEmpty() -> Loading
                        else -> Done
                    }
                }

                val items = fetchData(
                    filter = filterState.value ?: Completed,
                    page = 1,
                )

                itemsState.update { items }
                hasMoreData = items.size >= PROGRESS_PAGE_LIMIT
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

    fun loadMoreData() {
        if (itemsState.value.isNullOrEmpty() || !hasMoreData) return
        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) return

        viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                val nextPage = page + 1
                val filter = filterState.value ?: Completed
                val newItems = fetchData(filter, page = nextPage)

                itemsState.update { items ->
                    items
                        ?.plus(newItems)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                page = nextPage
                hasMoreData = newItems.size >= PROGRESS_PAGE_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { Done }
            }
        }
    }

    private suspend fun fetchLocalData(filter: ProgressFilter): ImmutableList<ProfileProgressItem> {
        return when (filter) {
            Completed -> getCompletedUseCase.getLocalCompleted(limit = PROGRESS_PAGE_LIMIT)
            InProgress -> getWatchingUseCase.getLocalWatching(limit = PROGRESS_PAGE_LIMIT)
            Dropped -> getDroppedUseCase.getLocalDropped(limit = PROGRESS_PAGE_LIMIT)
        }
    }

    private suspend fun fetchData(
        filter: ProgressFilter,
        page: Int,
    ): ImmutableList<ProfileProgressItem> {
        return when (filter) {
            Completed -> getCompletedUseCase.getCompleted(page = page, limit = PROGRESS_PAGE_LIMIT)
            InProgress -> getWatchingUseCase.getWatching(page = page, limit = PROGRESS_PAGE_LIMIT)
            Dropped -> getDroppedUseCase.getDropped(page = page, limit = PROGRESS_PAGE_LIMIT)
        }
    }

    fun setFilter(newFilter: ProgressFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            filterState.value = newFilter
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

    override fun onCleared() {
        loadDataJob?.cancel()
        loadDataJob = null
        super.onCleared()
    }

    val state = combine(
        loadingState,
        loadingMoreState,
        itemsState,
        filterState,
        navigateShow,
        userState,
        errorState,
    ) { states ->
        AllProgressState(
            loading = states[0] as LoadingState,
            loadingMore = states[1] as LoadingState,
            items = states[2] as? ImmutableList<ProfileProgressItem>,
            filter = states[3] as? ProgressFilter,
            navigateShow = states[4] as? TraktId,
            user = states[5] as? User,
            error = states[6] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
