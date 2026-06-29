package tv.trakt.trakt.core.home.sections.upnext.features.all

import android.content.Context
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
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates.Source.AllHomeUpNext
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_LIMIT
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.UpNextUpdates
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextMovie
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextShow
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.HISTORY
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.SEASON
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.resources.R

@OptIn(FlowPreview::class)
internal class AllHomeUpNextViewModel(
    private val appContext: Context,
    private val getUpNextUseCase: GetUpNextUseCase,
    private val updateHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val upNextUpdates: UpNextUpdates,
    private val showUpdates: ShowDetailsUpdates,
    private val episodeUpdates: EpisodeDetailsUpdates,
    private val movieUpdates: MovieDetailsUpdates,
    private val checkInUpdates: CheckInUpdates,
    private val checkInManager: CheckInManager,
    private val filterManager: GlobalFilterManager,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = AllHomeUpNextState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(Idle)
    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val userState = MutableStateFlow(initialState.user)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    private var pages: Int = 1
    private var hasMoreData: Boolean = true
    private var itemsOrder: List<Int>? = null

    init {
        loadUser()
        loadInitialData()

        observeData()
        observeFilter()

        analytics.logScreenView(
            screenName = "all_up_next",
        )
    }

    private fun observeFilter() {
        filterManager.observeFilter()
            .distinctUntilChanged()
            .onEach { value ->
                filterState.update { value }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        merge(
            showUpdates.observeUpdates(Source.PROGRESS),
            showUpdates.observeUpdates(Source.SEASONS),
            episodeUpdates.observeUpdates(PROGRESS),
            episodeUpdates.observeUpdates(SEASON),
            episodeUpdates.observeUpdates(HISTORY),
            movieUpdates.observeUpdates(MovieDetailsUpdates.Source.Progress),
            movieUpdates.observeUpdates(MovieDetailsUpdates.Source.History),
            checkInUpdates.observeUpdates().filterNot { it.first == AllHomeUpNext },
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach { loadData(ignoreErrors = true) }
            .launchIn(viewModelScope)
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadInitialData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) return@launch

                val localItems = getUpNextUseCase.getLocalUpNext(limit = HOME_ALL_LIMIT)
                    .filter {
                        when (filterState.value.mode) {
                            MediaMode.Shows -> it is UpNextShow
                            MediaMode.Movies -> it is UpNextMovie
                            else -> true
                        }
                    }.toImmutableList()

                itemsState
                    .update { localItems }
                    .also {
                        if (localItems.isEmpty()) {
                            loadingState.update { Loading }
                        }
                    }

                val remoteItems = getUpNextUseCase.getUpNext(
                    page = 1,
                    limit = HOME_ALL_LIMIT,
                    filters = filterState.value,
                    skipLocal = true,
                ).filter {
                    when (filterState.value.mode) {
                        MediaMode.Shows -> it is UpNextShow
                        MediaMode.Movies -> it is UpNextMovie
                        else -> true
                    }
                }.toImmutableList()

                itemsState
                    .update { remoteItems }
                    .also {
                        itemsOrder = itemsState.value?.map { it.mediaId.value }
                        hasMoreData = remoteItems.size >= HOME_ALL_LIMIT
                    }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        if (processingJob?.isActive == true) {
            return
        }

        pages = 1
        hasMoreData = true

        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val remoteItems = getUpNextUseCase.getUpNext(
                    page = 1,
                    limit = HOME_ALL_LIMIT,
                    filters = filterState.value,
                    skipLocal = true,
                ).filter {
                    when (filterState.value.mode) {
                        MediaMode.Shows -> it is UpNextShow
                        MediaMode.Movies -> it is UpNextMovie
                        else -> true
                    }
                }.toImmutableList()

                itemsState
                    .update { remoteItems }
                    .also {
                        itemsOrder = itemsState.value?.map { it.mediaId.value }
                        hasMoreData = remoteItems.size >= HOME_ALL_LIMIT
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
                dataJob = null
            }
        }
    }

    fun loadMoreData() {
        if (itemsState.value.isNullOrEmpty() || !hasMoreData) {
            return
        }
        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                val nextData = getUpNextUseCase.getUpNext(
                    page = pages + 1,
                    limit = HOME_ALL_LIMIT,
                    filters = filterState.value,
                    skipLocal = true,
                )

                itemsState.update { items ->
                    items
                        ?.plus(
                            nextData.filter {
                                when (filterState.value.mode) {
                                    MediaMode.Shows -> it is UpNextShow
                                    MediaMode.Movies -> it is UpNextMovie
                                    else -> true
                                }
                            },
                        )
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                pages += 1
                hasMoreData = nextData.size >= HOME_ALL_LIMIT

                itemsOrder = itemsState.value?.map { it.mediaId.value }
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

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            loadingState.update { Done }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { Idle }
        }

        return false
    }

    fun addToHistory(
        episodeId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        if (processingJob != null) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.id == episodeId }
                val itemLoading = (currentItems[itemIndex] as UpNextShow).copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    currentItems.toImmutableList()
                }

                updateHistoryUseCase.addToHistory(
                    episodeId = episodeId,
                    customDate = customDate,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "all_home_up_next",
                    date = customDate?.analyticsStrings,
                )

                itemsState.update {
                    val items = getUpNextUseCase.getUpNext(
                        page = 1,
                        limit = HOME_ALL_LIMIT,
                        filters = filterState.value,
                        skipLocal = true,
                    )
                    itemsOrder?.let { order ->
                        items
                            .sortedBy { order.indexOf(it.mediaId.value) }
                            .toImmutableList()
                    } ?: items
                }

                upNextUpdates.notifyUpdate()
                loadUserProgress()

                infoState.update { DynamicStringResource(R.string.text_info_history_added) }
                itemsOrder = itemsState.value?.map { it.mediaId.value }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun addEpisodeCheckIn(
        showId: TraktId,
        episodeId: TraktId,
        seasonEpisode: SeasonEpisode,
    ) {
        if (processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.id == episodeId }
                val itemLoading = (currentItems[itemIndex] as UpNextShow).copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    currentItems.toImmutableList()
                }

                checkInManager.startEpisode(
                    context = appContext,
                    showId = showId,
                    seasonEpisode = seasonEpisode,
                    source = AllHomeUpNext,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "all_home_up_next",
                    date = "checkin",
                )

                itemsState.update {
                    val items = getUpNextUseCase.getUpNext(
                        page = 1,
                        limit = HOME_ALL_LIMIT,
                        filters = filterState.value,
                        skipLocal = true,
                    )
                    itemsOrder?.let { order ->
                        items
                            .sortedBy { order.indexOf(it.mediaId.value) }
                            .toImmutableList()
                    } ?: items
                }

                itemsOrder = itemsState.value?.map { it.mediaId.value }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun loadUserProgress() {
        viewModelScope.launch {
            try {
                loadUserProgressUseCase.loadShowsProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun setFilter(newFilter: GlobalFilter) {
        if (newFilter == filterState.value) {
            return
        }
        filterState.update { newFilter }
        loadData()
    }

    fun removeShow(showId: TraktId) {
        itemsState.update { items ->
            items
                ?.filter { it.mediaId != showId || it !is UpNextShow }
                ?.toImmutableList()
        }

        itemsOrder = itemsState.value
            ?.map { it.mediaId.value }
    }

    fun removeMovie(movieId: TraktId) {
        itemsState.update { items ->
            items
                ?.filter { it.mediaId != movieId || it !is UpNextMovie }
                ?.toImmutableList()
        }

        itemsOrder = itemsState.value
            ?.map { it.mediaId.value }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        itemsState,
        loadingState,
        loadingMoreState,
        filterState,
        userState,
        infoState,
        errorState,
    ) { state ->
        AllHomeUpNextState(
            items = state[0] as ImmutableList<UpNextShow>?,
            loading = state[1] as LoadingState,
            loadingMore = state[2] as LoadingState,
            filter = state[3] as GlobalFilter,
            user = state[4] as User?,
            info = state[5] as StringResource?,
            error = state[6] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
