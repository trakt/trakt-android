package tv.trakt.trakt.core.lists.sections.watchlist.features.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
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
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StaticStringResource
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.watchlist.usecases.AddHomeHistoryUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.data.AllWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.data.AllWatchlistLocalDataSource.Source.ALL
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
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase

@OptIn(FlowPreview::class)
internal class AllWatchlistViewModel(
    modeManager: MediaModeManager,
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val getShowsWatchlistUseCase: GetShowsWatchlistUseCase,
    private val getMoviesWatchlistUseCase: GetMoviesWatchlistUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val updateMovieHistoryUseCase: AddHomeHistoryUseCase,
    private val allWatchlistLocalDataSource: AllWatchlistLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val showUpdatesSource: ShowDetailsUpdates,
    private val episodeUpdatesSource: EpisodeDetailsUpdates,
    private val movieDetailsUpdates: MovieDetailsUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = AllWatchlistState()
    private val initialMode = modeManager.getMode()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialMode)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadBackground()
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

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    fun loadData(
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val localItems = when (filterState.value) {
                    MEDIA -> getWatchlistUseCase.getLocalWatchlist()
                    SHOWS -> getShowsWatchlistUseCase.getLocalWatchlist()
                    MOVIES -> getMoviesWatchlistUseCase.getLocalWatchlist()
                }

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                    if (localOnly) {
                        return@launch
                    }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    when (filterState.value) {
                        MEDIA -> getWatchlistUseCase.getWatchlist()
                        SHOWS -> getShowsWatchlistUseCase.getWatchlist()
                        MOVIES -> getMoviesWatchlistUseCase.getWatchlist()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.e(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<WatchlistItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    fun setFilter(newFilter: MediaMode) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            filterState.update { newFilter }
            loadData(localOnly = true)
        }
    }

    fun addMovieToHistory(movieId: TraktId) {
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

                updateMovieHistoryUseCase.addMovieToHistory(movieId)
                removeItem(currentItems[itemIndex])
                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "all_watchlist",
                )

                infoState.update {
                    StaticStringResource("Added to history")
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

        allWatchlistLocalDataSource.notifyUpdate(ALL)
    }

    fun loadUserProgress() {
        viewModelScope.launch {
            try {
                loadUserProgressUseCase.loadMoviesProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
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
        loadDataJob?.cancel()
        loadDataJob = null

        processingJob?.cancel()
        processingJob = null

        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        itemsState,
        filterState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        infoState,
        errorState,
        backgroundState,
    ) { state ->
        AllWatchlistState(
            loading = state[0] as LoadingState,
            items = state[1] as? ImmutableList<WatchlistItem>,
            filter = state[2] as? MediaMode,
            collection = state[3] as UserCollectionState,
            navigateShow = state[4] as? TraktId,
            navigateMovie = state[5] as? TraktId,
            info = state[6] as? StringResource,
            error = state[7] as? Exception,
            backgroundUrl = state[8] as? String,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
