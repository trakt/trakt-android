@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.home.sections.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StaticStringResource
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.HomeConfig.HOME_WATCHLIST_LIMIT
import tv.trakt.trakt.core.home.sections.watchlist.usecases.AddHomeHistoryUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeMoviesWatchlistUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.MovieItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.ShowItem
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import java.time.Instant

@OptIn(FlowPreview::class)
internal class HomeWatchlistViewModel(
    private val getMoviesUseCase: GetHomeMoviesWatchlistUseCase,
    private val getShowsUseCase: GetHomeShowsWatchlistUseCase,
    private val addHistoryUseCase: AddHomeHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val userWatchlistSource: UserWatchlistLocalDataSource,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val modeManager: MediaModeManager,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = HomeWatchlistState()
    private val initialMode = modeManager.getMode()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialMode)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var user: User? = null
    private var loadedAt: Instant? = null
    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadData()
        observeMode()
        observeUser()
        observeData()
    }

    private fun observeUser() {
        viewModelScope.launch {
            user = sessionManager.getProfile()
            sessionManager.observeProfile()
                .drop(1)
                .distinctUntilChanged()
                .debounce(200)
                .collect {
                    user = it
                    loadData()
                }
        }
    }

    private fun observeMode() {
        modeManager.observeMode()
            .distinctUntilChanged()
            .onEach { value ->
                filterState.update { value }
                loadData(localOnly = true)
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        userWatchlistSource.observeUpdates()
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(
                    localOnly = false,
                    ignoreErrors = true,
                )
            }
            .launchIn(viewModelScope)
    }

    fun loadData(
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                coroutineScope {
                    val localShowsAsync = async { getShowsUseCase.getLocalWatchlist(HOME_WATCHLIST_LIMIT) }
                    val localMoviesAsync = async { getMoviesUseCase.getLocalWatchlist(HOME_WATCHLIST_LIMIT) }

                    val (localShows, localMovies) = awaitAll(localShowsAsync, localMoviesAsync)

                    if (localMovies.isNotEmpty() || localShows.isNotEmpty()) {
                        itemsState.update {
                            (localShows + localMovies)
                                .filter {
                                    when (filterState.value) {
                                        MediaMode.SHOWS -> it is ShowItem
                                        MediaMode.MOVIES -> it is MovieItem
                                        else -> true
                                    }
                                }
                                .sortedWith(
                                    compareByDescending<WatchlistItem> { it.released }
                                        .thenBy { it.title },
                                )
                                .toImmutableList()
                        }
                        loadingState.update { DONE }
                    } else {
                        loadingState.update { LOADING }
                    }
                }

                if (localOnly) {
                    return@launch
                }

                coroutineScope {
                    val showsAsync = async { getShowsUseCase.getWatchlist(HOME_WATCHLIST_LIMIT) }
                    val moviesAsync = async { getMoviesUseCase.getWatchlist(HOME_WATCHLIST_LIMIT) }

                    itemsState.update {
                        (showsAsync.await() + moviesAsync.await())
                            .filter {
                                when (filterState.value) {
                                    MediaMode.SHOWS -> it is ShowItem
                                    MediaMode.MOVIES -> it is MovieItem
                                    else -> true
                                }
                            }
                            .sortedWith(
                                compareByDescending<WatchlistItem> { it.released }
                                    .thenBy { it.title },
                            )
                            .toImmutableList()
                    }
                }

                loadedAt = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
                dataJob = null
            }
        }
    }

    fun addShowToHistory(
        showId: TraktId,
        episodeId: TraktId?,
    ) {
        if (processingJob?.isActive == true || episodeId == null) {
            return
        }

        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value?.toMutableList() ?: return@launch

                val itemIndex = currentItems
                    .indexOfFirst {
                        it is ShowItem &&
                            it.show.ids.trakt == showId
                    }
                val itemLoading = (currentItems[itemIndex] as ShowItem)
                    .copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    currentItems.toImmutableList()
                }

                addHistoryUseCase.addEpisodeToHistory(
                    showId = showId,
                    episodeId = episodeId,
                )
                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "home_watchlist",
                )

                infoState.update {
                    StaticStringResource("Added to history")
                }

                loadShowsProgress()

                loadedAt = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.d(error, "Failed to add episode to history")
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun addMovieToHistory(movieId: TraktId) {
        if (processingJob?.isActive == true) {
            return
        }

        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value?.toMutableList() ?: return@launch

                val itemIndex = currentItems
                    .indexOfFirst {
                        it is MovieItem &&
                            it.movie.ids.trakt == movieId
                    }
                val itemLoading = (currentItems[itemIndex] as MovieItem)
                    .copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    currentItems.toImmutableList()
                }

                addHistoryUseCase.addMovieToHistory(movieId)
                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "home_watchlist",
                )

                infoState.update {
                    StaticStringResource("Added to history")
                }

                loadMoviesProgress()

                loadedAt = nowUtcInstant()
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

    fun loadShowsProgress() {
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

    fun loadMoviesProgress() {
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

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            loadingState.update { DONE }
            return true
        } else {
            loadingState.update { IDLE }
        }

        return false
    }

    override fun onCleared() {
        processingJob?.cancel()
        processingJob = null
        super.onCleared()
    }

    val state = combine(
        itemsState,
        filterState,
        loadingState,
        infoState,
        errorState,
        navigateMovie,
        navigateShow,
    ) { state ->
        HomeWatchlistState(
            items = state[0] as ImmutableList<WatchlistItem>?,
            filter = state[1] as MediaMode,
            loading = state[2] as LoadingState,
            info = state[3] as StringResource?,
            error = state[4] as Exception?,
            navigateMovie = state[5] as TraktId?,
            navigateShow = state[6] as TraktId?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
