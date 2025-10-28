package tv.trakt.trakt.core.home.sections.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StaticStringResource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.HomeConfig.HOME_WATCHLIST_LIMIT
import tv.trakt.trakt.core.home.sections.watchlist.usecases.AddHomeHistoryUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.data.AllWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import java.time.Instant

@OptIn(FlowPreview::class)
internal class HomeWatchlistViewModel(
    private val getWatchlistUseCase: GetHomeWatchlistUseCase,
    private val addHistoryUseCase: AddHomeHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val allWatchlistSource: AllWatchlistLocalDataSource,
    private val userWatchlistSource: UserWatchlistLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = HomeWatchlistState()

    private val itemsState = MutableStateFlow(initialState.items)
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
        observeUser()
        observeWatchlist()
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

    private fun observeWatchlist() {
        merge(
            allWatchlistSource.observeUpdates(),
            userWatchlistSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(
                    ignoreErrors = true,
                    localOnly = true,
                )
            }
            .launchIn(viewModelScope)
    }

    fun loadData(
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        if (dataJob?.isActive == true) return
        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localItems = getWatchlistUseCase.getLocalWatchlist(HOME_WATCHLIST_LIMIT)
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
                    getWatchlistUseCase.getWatchlist(HOME_WATCHLIST_LIMIT)
                }

                loadedAt = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.d(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
                dataJob = null
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<WatchlistItem.MovieItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        } else {
            loadingState.update { IDLE }
        }

        return false
    }

    fun addToHistory(movieId: TraktId) {
        if (processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.movie.ids.trakt == movieId }
                val itemLoading = currentItems[itemIndex].copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    currentItems.toImmutableList()
                }

                addHistoryUseCase.addToHistory(movieId)

                infoState.update {
                    StaticStringResource("Added to history")
                }

                itemsState.update {
                    getWatchlistUseCase.getWatchlist()
                }

                loadUserProgress()

                loadedAt = nowUtcInstant()
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

    val state: StateFlow<HomeWatchlistState> = combine(
        itemsState,
        loadingState,
        infoState,
        errorState,
        navigateMovie,
    ) { s1, s2, s3, s4, s5 ->
        HomeWatchlistState(
            items = s1,
            loading = s2,
            info = s3,
            error = s4,
            navigateMovie = s5,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
