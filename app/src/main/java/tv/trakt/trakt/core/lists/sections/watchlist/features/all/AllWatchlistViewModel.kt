package tv.trakt.trakt.core.lists.sections.watchlist.features.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeWatchlistUseCase
import tv.trakt.trakt.core.lists.model.ListsMediaFilter
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.MEDIA
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.MOVIES
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.SHOWS
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.data.AllWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.ListsWatchlistDestination
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.MovieItem
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.filters.GetWatchlistFilterUseCase
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase

@OptIn(FlowPreview::class)
internal class AllWatchlistViewModel(
    savedStateHandle: SavedStateHandle,
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val getHomeWatchlistUseCase: GetHomeWatchlistUseCase,
    private val getShowsWatchlistUseCase: GetShowsWatchlistUseCase,
    private val getMoviesWatchlistUseCase: GetMoviesWatchlistUseCase,
    private val getFilterUseCase: GetWatchlistFilterUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val updateMovieHistoryUseCase: AddHomeHistoryUseCase,
    private val allWatchlistLocalDataSource: AllWatchlistLocalDataSource,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val showUpdatesSource: ShowDetailsUpdates,
    private val episodeUpdatesSource: EpisodeDetailsUpdates,
    private val movieDetailsUpdates: MovieDetailsUpdates,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ListsWatchlistDestination>()

    private val initialState = AllWatchlistState()

    private val isHomeWatchlist = MutableStateFlow(destination.homeWatchlist)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
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

        analytics.logScreenView(
            screenName = "AllWatchlist",
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

                val filter = when {
                    !destination.homeWatchlist -> loadFilter()
                    else -> null
                }

                val localItems = when {
                    destination.homeWatchlist -> {
                        getHomeWatchlistUseCase.getLocalWatchlist()
                    }
                    else -> when (filter) {
                        MEDIA -> getWatchlistUseCase.getLocalWatchlist()
                        SHOWS -> getShowsWatchlistUseCase.getLocalWatchlist()
                        MOVIES -> getMoviesWatchlistUseCase.getLocalWatchlist()
                        else -> emptyList<WatchlistItem>().toImmutableList()
                    }
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
                    when {
                        destination.homeWatchlist -> {
                            getHomeWatchlistUseCase.getWatchlist()
                        }
                        else -> when (filter) {
                            MEDIA -> getWatchlistUseCase.getWatchlist()
                            SHOWS -> getShowsWatchlistUseCase.getWatchlist()
                            MOVIES -> getMoviesWatchlistUseCase.getWatchlist()
                            else -> emptyList<WatchlistItem>().toImmutableList()
                        }
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadFilter(): ListsMediaFilter {
        val filter = getFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
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

    fun setFilter(newFilter: ListsMediaFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getFilterUseCase.setFilter(newFilter)
            loadFilter()
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

                updateMovieHistoryUseCase.addToHistory(movieId)
                removeItem(currentItems[itemIndex])

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

        allWatchlistLocalDataSource.notifyUpdate()
    }

    fun loadUserProgress() {
        viewModelScope.launch {
            try {
                loadUserProgressUseCase.loadMoviesProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
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
    val state: StateFlow<AllWatchlistState> = combine(
        loadingState,
        itemsState,
        filterState,
        navigateShow,
        navigateMovie,
        infoState,
        errorState,
        backgroundState,
        isHomeWatchlist,
    ) { state ->
        AllWatchlistState(
            loading = state[0] as LoadingState,
            items = state[1] as? ImmutableList<WatchlistItem>,
            filter = state[2] as? ListsMediaFilter,
            navigateShow = state[3] as? TraktId,
            navigateMovie = state[4] as? TraktId,
            info = state[5] as? StringResource,
            error = state[6] as? Exception,
            backgroundUrl = state[7] as? String,
            isHomeWatchlist = state[8] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
