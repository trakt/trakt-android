package tv.trakt.trakt.core.lists.sections.watchlist.features.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.home.sections.watchlist.usecases.AddHomeHistoryUseCase
import tv.trakt.trakt.core.lists.ListsConfig.WATCHLIST_PAGE_LIMIT
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.MovieItem
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source.AllWatchlist
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

@OptIn(FlowPreview::class)
internal class AllWatchlistViewModel(
    modeManager: MediaModeManager,
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val getShowsWatchlistUseCase: GetShowsWatchlistUseCase,
    private val getMoviesWatchlistUseCase: GetMoviesWatchlistUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val updateMovieHistoryUseCase: AddHomeHistoryUseCase,
    private val collectionStateProvider: CollectionStateProvider,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val showUpdatesSource: ShowDetailsUpdates,
    private val episodeUpdatesSource: EpisodeDetailsUpdates,
    private val movieDetailsUpdates: MovieDetailsUpdates,
    private val watchlistUpdates: WatchlistUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = AllWatchlistState()
    private val initialMode = modeManager.getMode()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialMode)
    private val sortingState = MutableStateFlow(initialState.sorting)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null
    private var loadingJob: Job? = null

    private var page: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadUser()
        loadData()

        observeData()
        observeCollection()

        analytics.logScreenView(
            screenName = "all_watchlist",
        )
    }

    private fun observeData() {
        merge(
            showUpdatesSource.observeUpdates(Source.PROGRESS),
            showUpdatesSource.observeUpdates(Source.SEASONS),
            episodeUpdatesSource.observeUpdates(EpisodeDetailsUpdates.Source.PROGRESS),
            episodeUpdatesSource.observeUpdates(EpisodeDetailsUpdates.Source.SEASON),
            movieDetailsUpdates.observeUpdates(),
            watchlistUpdates.observeUpdates(Default),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    fun loadUser() {
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

    fun loadData(ignoreErrors: Boolean = false) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                page = 1
                hasMoreData = true

                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val filter = filterState.value
                val sorting = sortingState.value

                loadingJob = launch {
                    if (itemsState.value?.isNotEmpty() == true) {
                        // Avoid blinking loading but still show it if loading takes too long
                        delay(100)
                    }
                    loadingState.update { Loading }
                }

                itemsState.update {
                    when (filter) {
                        MEDIA -> getWatchlistUseCase.getRemoteWatchlist(
                            page = page,
                            limit = WATCHLIST_PAGE_LIMIT,
                            sorting = sorting,
                            skipLocal = true,
                        )
                        SHOWS -> getShowsWatchlistUseCase.getRemoteWatchlist(
                            page = page,
                            limit = WATCHLIST_PAGE_LIMIT,
                            sorting = sorting,
                            skipLocal = true,
                        )
                        MOVIES -> getMoviesWatchlistUseCase.getRemoteWatchlist(
                            page = page,
                            limit = WATCHLIST_PAGE_LIMIT,
                            sorting = sorting,
                            skipLocal = true,
                        )
                    }
                }

                hasMoreData = (itemsState.value?.size ?: 0) >= WATCHLIST_PAGE_LIMIT
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
                loadingJob?.cancel()
                loadingJob = null
            }
        }
    }

    fun loadMoreData() {
        if (loadingState.value.isLoading ||
            loadingMoreState.value.isLoading ||
            dataJob?.isActive == true ||
            !hasMoreData ||
            (itemsState.value?.size ?: 0) < WATCHLIST_PAGE_LIMIT
        ) {
            return
        }

        dataJob = viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }
                val newItems = when (filterState.value) {
                    MEDIA -> getWatchlistUseCase.getRemoteWatchlist(
                        page = page + 1,
                        limit = WATCHLIST_PAGE_LIMIT,
                        sorting = sortingState.value,
                        skipLocal = true,
                    )
                    SHOWS -> getShowsWatchlistUseCase.getRemoteWatchlist(
                        page = page + 1,
                        limit = WATCHLIST_PAGE_LIMIT,
                        sorting = sortingState.value,
                        skipLocal = true,
                    )
                    MOVIES -> getMoviesWatchlistUseCase.getRemoteWatchlist(
                        page = page + 1,
                        limit = WATCHLIST_PAGE_LIMIT,
                        sorting = sortingState.value,
                        skipLocal = true,
                    )
                }

                itemsState.update { items ->
                    items
                        ?.plus(newItems)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                page += 1
                hasMoreData = (newItems.size >= WATCHLIST_PAGE_LIMIT)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingMoreState.update { Done }
                dataJob = null
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<WatchlistItem>().toImmutableList()
            }
            loadingState.update { Done }
            return true
        }

        return false
    }

    fun setFilter(newFilter: MediaMode) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        filterState.update { newFilter }
        loadData()
    }

    fun setSorting(newSorting: Sorting) {
        if (newSorting == sortingState.value || loadingState.value.isLoading) {
            return
        }

        sortingState.update {
            it.copy(
                type = newSorting.type,
                order = newSorting.order,
            )
        }

        loadData()
    }

    fun addMovieToHistory(
        movieId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        if (processingJob != null) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.id == movieId && (it is MovieItem) }
                val itemLoading = (currentItems[itemIndex] as MovieItem).copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    currentItems.toImmutableList()
                }

                updateMovieHistoryUseCase.addMovieToHistory(
                    movieId = movieId,
                    customDate = customDate,
                )
                removeItem(currentItems[itemIndex])
                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "all_watchlist",
                    date = customDate?.analyticsStrings,
                )

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }

                loadUserProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.d(error, "Failed to add movie to history")
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun removeItem(item: WatchlistItem?) {
        val currentItems = itemsState.value ?: return

        itemsState.update {
            currentItems
                .filterNot {
                    it.id == item?.id &&
                        it.type == item.type
                }
                .toImmutableList()
        }

        watchlistUpdates.notifyUpdate(AllWatchlist)
    }

    fun loadUserProgress() {
        viewModelScope.launch {
            try {
                loadUserProgressUseCase.loadMoviesProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    override fun onCleared() {
        dataJob?.cancel()
        dataJob = null

        processingJob?.cancel()
        processingJob = null

        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        loadingMoreState,
        itemsState,
        filterState,
        sortingState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        userState,
        infoState,
        errorState,
    ) { state ->
        AllWatchlistState(
            loading = state[0] as LoadingState,
            loadingMore = state[1] as LoadingState,
            items = state[2] as? ImmutableList<WatchlistItem>,
            filter = state[3] as? MediaMode,
            sorting = state[4] as Sorting,
            collection = state[5] as UserCollectionState,
            navigateShow = state[6] as? TraktId,
            navigateMovie = state[7] as? TraktId,
            user = state[8] as? User,
            info = state[9] as? StringResource,
            error = state[10] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
