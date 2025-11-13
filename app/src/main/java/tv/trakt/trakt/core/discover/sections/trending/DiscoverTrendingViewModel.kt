@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.discover.sections.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.interleave
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase

internal class DiscoverTrendingViewModel(
    private val sessionManager: SessionManager,
    private val modeManager: MediaModeManager,
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val userProgressUseCase: LoadUserProgressUseCase,
    private val userWatchlistUseCase: LoadUserWatchlistUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
) : ViewModel() {
    private val initialState = DiscoverTrendingState()

    private val modeState = MutableStateFlow(modeManager.getMode())
    private val itemsState = MutableStateFlow(initialState.items)
    private val watchedItemsState = MutableStateFlow(initialState.watchedItems)
    private val watchlistItemsState = MutableStateFlow(initialState.watchlistItems)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null

    init {
        loadData()
        loadWatchlistData()

        observeMode()
        observeWatchlistData()
    }

    private fun observeMode() {
        modeManager.observeMode()
            .onEach { value ->
                modeState.update { value }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeWatchlistData() {
        userWatchlistLocalSource
            .observeUpdates()
            .distinctUntilChanged()
            .onEach {
                loadWatchlistData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadLocalData()
                loadRemoteData()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
                dataJob = null
            }
        }

        Timber.d("Loading trending data for mode: ${modeState.value}")
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val localShowsAsync = async { getTrendingShowsUseCase.getLocalShows() }
            val localMoviesAsync = async { getTrendingMoviesUseCase.getLocalMovies() }

            val localShows = if (modeState.value.isMediaOrShows) localShowsAsync.await() else emptyList()
            val localMovies = if (modeState.value.isMediaOrMovies) localMoviesAsync.await() else emptyList()

            if (localShows.isNotEmpty() || localMovies.isNotEmpty()) {
                itemsState.update {
                    listOf(localShows, localMovies)
                        .interleave()
                        .toImmutableList()
                }
                loadingState.update { DONE }
            } else {
                loadingState.update { LOADING }
            }
        }
    }

    private suspend fun loadRemoteData() {
        return coroutineScope {
            val showsAsync = async { getTrendingShowsUseCase.getShows() }
            val moviesAsync = async { getTrendingMoviesUseCase.getMovies() }

            val shows = if (modeState.value.isMediaOrShows) showsAsync.await() else emptyList()
            val movies = if (modeState.value.isMediaOrMovies) moviesAsync.await() else emptyList()

            itemsState.update {
                listOf(shows, movies)
                    .interleave()
                    .toImmutableList()
            }
        }
    }

    private fun loadWatchlistData() {
        val usecase = userWatchlistUseCase
        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    watchedItemsState.update { EmptyImmutableSet }
                    watchlistItemsState.update { EmptyImmutableSet }
                    return@launch
                }

                coroutineScope {
                    val watchlistShowsAsync = async { usecase.loadLocalShows() }
                    val watchlistMoviesAsync = async { usecase.loadLocalMovies() }

                    val watchlistShows = when {
                        modeState.value.isMediaOrShows -> watchlistShowsAsync.await()
                        else -> emptySet()
                    }
                    val watchlistMovies = when {
                        modeState.value.isMediaOrMovies -> watchlistMoviesAsync.await()
                        else -> emptySet()
                    }

                    watchlistItemsState.update {
                        (watchlistShows + watchlistMovies)
                            .asyncMap { it.key }
                            .toImmutableSet()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Failed to load watchlist data")
                }
            }
        }
    }

    val state = combine(
        itemsState,
        watchedItemsState,
        watchlistItemsState,
        modeState,
        loadingState,
        errorState,
    ) { state ->
        DiscoverTrendingState(
            items = state[0] as ImmutableList<DiscoverItem>?,
            watchedItems = state[1] as ImmutableSet<String>,
            watchlistItems = state[2] as ImmutableSet<String>,
            mode = state[3] as MediaMode?,
            loading = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
