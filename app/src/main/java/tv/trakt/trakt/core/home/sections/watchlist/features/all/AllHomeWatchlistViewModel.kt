@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.home.sections.watchlist.features.all

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates.Source
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_WATCHLIST_LIMIT
import tv.trakt.trakt.core.home.HomeConfig.HOME_WATCHLIST_LIMIT
import tv.trakt.trakt.core.home.sections.watchlist.usecases.AddHomeHistoryUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeMoviesWatchlistUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.MovieItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.ShowItem
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source.AllWatchlist
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.resources.R

@OptIn(FlowPreview::class)
internal class AllHomeWatchlistViewModel(
    private val appContext: Context,
    private val getMoviesUseCase: GetHomeMoviesWatchlistUseCase,
    private val getShowsUseCase: GetHomeShowsWatchlistUseCase,
    private val addHistoryUseCase: AddHomeHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val userWatchlistSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinSource: UserWatchlistMinimalLocalDataSource,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val filterManager: GlobalFilterManager,
    private val checkInUpdates: CheckInUpdates,
    private val watchlistUpdates: WatchlistUpdates,
    private val checkInManager: CheckInManager,
    private val sessionManager: SessionManager,
    private val ratePromptManager: RatePromptManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = AllHomeWatchlistState()
    private val initialMode = filterManager.getFilter()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialMode)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val userState = MutableStateFlow(initialState.user)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadInitialData()

        observeFilter()
        observeUser()
        observeData()
    }

    private fun observeUser() {
        viewModelScope.launch {
            userState.update { sessionManager.getProfile() }
            sessionManager.observeProfile()
                .drop(1)
                .distinctUntilChanged()
                .debounce(200)
                .onStart { }
                .collect { user ->
                    userState.update { user }
                    loadData()
                }
        }
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
            watchlistUpdates.observeUpdates(Default),
            checkInUpdates.observeUpdates()
                .filter { it.first != Source.AllHomeWatchlist },
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach { loadData(ignoreErrors = true) }
            .launchIn(viewModelScope)
    }

    private fun loadInitialData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) return@launch

            try {
                val localShowsAsync = async { getShowsUseCase.getLocalWatchlist(HOME_WATCHLIST_LIMIT) }
                val localMoviesAsync = async { getMoviesUseCase.getLocalWatchlist(HOME_WATCHLIST_LIMIT) }
                val (localShows, localMovies) = awaitAll(localShowsAsync, localMoviesAsync)

                itemsState.update {
                    (localShows + localMovies)
                        .filter {
                            when (filterState.value.mode) {
                                MediaMode.Shows -> it is ShowItem
                                MediaMode.Movies -> it is MovieItem
                                else -> true
                            }
                        }
                        .sortedWith(
                            compareByDescending<WatchlistItem> { it.released }
                                .thenByDescending { it.listedAt },
                        )
                        .distinctBy { it.key }
                        .toImmutableList()
                }.also {
                    if (localShows.isEmpty() && localMovies.isEmpty()) {
                        loadingState.update { Loading }
                    }
                }

                val showsAsync = async {
                    getShowsUseCase.getWatchlist(
                        limit = HOME_ALL_WATCHLIST_LIMIT,
                        filters = filterState.value,
                        skipLocal = true,
                    )
                }
                val moviesAsync = async {
                    getMoviesUseCase.getWatchlist(
                        limit = HOME_ALL_WATCHLIST_LIMIT,
                        filters = filterState.value,
                        skipLocal = true,
                    )
                }

                itemsState.update {
                    (showsAsync.await() + moviesAsync.await())
                        .filter {
                            when (filterState.value.mode) {
                                MediaMode.Shows -> it is ShowItem
                                MediaMode.Movies -> it is MovieItem
                                else -> true
                            }
                        }
                        .sortedWith(
                            compareByDescending<WatchlistItem> { it.released }
                                .thenByDescending { it.listedAt },
                        )
                        .distinctBy { it.key }
                        .toImmutableList()
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

        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val showsAsync = async {
                    getShowsUseCase.getWatchlist(
                        limit = HOME_ALL_WATCHLIST_LIMIT,
                        filters = filterState.value,
                        skipLocal = true,
                    )
                }
                val moviesAsync = async {
                    getMoviesUseCase.getWatchlist(
                        limit = HOME_ALL_WATCHLIST_LIMIT,
                        filters = filterState.value,
                        skipLocal = true,
                    )
                }

                itemsState.update {
                    (showsAsync.await() + moviesAsync.await())
                        .filter {
                            when (filterState.value.mode) {
                                MediaMode.Shows -> it is ShowItem
                                MediaMode.Movies -> it is MovieItem
                                else -> true
                            }
                        }
                        .sortedWith(
                            compareByDescending<WatchlistItem> { it.released }
                                .thenByDescending { it.listedAt },
                        )
                        .distinctBy { it.key }
                        .toImmutableList()
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

    fun addShowToHistory(
        showId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        if (processingJob?.isActive == true) {
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
                    seasonEpisode = SeasonEpisode(1, 1),
                    customDate = customDate,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "all_home_watchlist",
                    date = customDate?.analyticsStrings,
                )

                removeItem(
                    item = currentItems[itemIndex],
                    notify = true,
                )

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }

                loadShowsProgress()
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

    fun addMovieToHistory(
        movieId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
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

                addHistoryUseCase.addMovieToHistory(
                    movieId = movieId,
                    customDate = customDate,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "all_home_watchlist",
                    date = customDate?.analyticsStrings,
                )

                removeItem(
                    item = currentItems[itemIndex],
                    notify = true,
                )

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }

                loadMoviesProgress()
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
        seasonEpisode: SeasonEpisode,
    ) {
        if (processingJob?.isActive == true) {
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

                checkInManager.startEpisode(
                    showId = showId,
                    seasonEpisode = seasonEpisode,
                    source = Source.AllHomeWatchlist,
                    context = appContext,
                )

                removeItem(
                    item = currentItems[itemIndex],
                    notify = false,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "all_home_watchlist",
                    date = "checkin",
                )
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

    fun addMovieCheckIn(movieId: TraktId) {
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

                checkInManager.startMovie(
                    movieId = movieId,
                    source = Source.AllHomeWatchlist,
                    context = appContext,
                )
                removeItem(currentItems[itemIndex], notify = false)

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "all_home_watchlist",
                    date = "checkin",
                )
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

    fun removeItem(
        item: WatchlistItem?,
        notify: Boolean = false,
    ) {
        val currentItems = itemsState.value ?: return

        itemsState.update {
            currentItems
                .filterNot { it.key == item?.key }
                .toImmutableList()
        }

        viewModelScope.launch {
            when (item) {
                is ShowItem -> {
                    userWatchlistSource.removeShows(setOf(item.id))
                    userWatchlistMinSource.removeShows(setOf(item.id))
                }
                is MovieItem -> {
                    userWatchlistSource.removeMovies(setOf(item.id))
                    userWatchlistMinSource.removeMovies(setOf(item.id))
                }
                else -> {}
            }

            if (notify) {
                watchlistUpdates.notifyUpdate(AllWatchlist)
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
                ratePromptManager.checkMovies()
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

    fun setFilter(newFilter: GlobalFilter) {
        if (newFilter == filterState.value) {
            return
        }
        filterState.update { newFilter }
        loadData()
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    fun clearInfo() {
        infoState.update { null }
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
        userState,
        infoState,
        errorState,
        navigateMovie,
        navigateShow,
    ) { state ->
        AllHomeWatchlistState(
            items = state[0] as ImmutableList<WatchlistItem>?,
            filter = state[1] as GlobalFilter?,
            loading = state[2] as LoadingState,
            user = state[3] as User?,
            info = state[4] as StringResource?,
            error = state[5] as Exception?,
            navigateMovie = state[6] as TraktId?,
            navigateShow = state[7] as TraktId?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
