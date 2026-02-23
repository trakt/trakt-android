@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.app.core.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.Config.REFRESH_DATA_THRESHOLD_MINUTES
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.app.core.lists.usecases.GetListsLikedUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsMoviesWatchlistUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsPersonalUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsShowsWatchlistUseCase
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.time.ZonedDateTime

internal class ListsViewModel(
    private val getShowsWatchlistUseCase: GetListsShowsWatchlistUseCase,
    private val getMoviesWatchlistUseCase: GetListsMoviesWatchlistUseCase,
    private val getPersonalUseCase: GetListsPersonalUseCase,
    private val getLikedUseCase: GetListsLikedUseCase,
    private val showsLocalSyncSource: ShowsSyncLocalDataSource,
    private val moviesLocalSyncSource: MoviesSyncLocalDataSource,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val cacheMarkerProvider: CacheMarkerProvider,
) : ViewModel() {
    private val initialState = ListsState()

    private val showsState = MutableStateFlow(initialState.watchlistShows)
    private val moviesState = MutableStateFlow(initialState.watchlistMovies)
    private val listsPersonalState = MutableStateFlow(initialState.personalLists)
    private val listsLikedState = MutableStateFlow(initialState.likedLists)
    private val loadingState = MutableStateFlow(initialState.loadingLists)
    private val errorState = MutableStateFlow(initialState.error)

    private var showsLoadedAt: ZonedDateTime? = null
    private var moviesLoadedAt: ZonedDateTime? = null
    private var marker: String? = null

    init {
        loadWatchlistData()
        loadPersonalListsData()
        loadLikedListsData()

        observeApp()
    }

    private fun observeApp() {
        appLifecycleProvider.observeState(FOREGROUND)
            .filter {
                val threshold = nowUtc().minusMinutes(REFRESH_DATA_THRESHOLD_MINUTES)

                val showsNeedLoad = showsLoadedAt != null && threshold.isAfter(showsLoadedAt)
                val moviesNeedLoad = moviesLoadedAt != null && threshold.isAfter(moviesLoadedAt)

                showsNeedLoad || moviesNeedLoad
            }
            .onEach {
                loadWatchlistData(showLoading = false)
            }
            .launchIn(viewModelScope)
    }

    private fun loadWatchlistData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update {
                        it.copy(loadingWatchlist = true)
                    }
                }

                coroutineScope {
                    val showsAsync = async {
                        getShowsWatchlistUseCase.getShows(
                            limit = LISTS_SECTION_LIMIT,
                        )
                    }
                    val moviesAsync = async {
                        getMoviesWatchlistUseCase.getMovies(
                            limit = LISTS_SECTION_LIMIT,
                        )
                    }

                    showsState.update { showsAsync.await() }
                    moviesState.update { moviesAsync.await() }
                }

                showsLoadedAt = nowUtc()
                moviesLoadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading: ${error.message}")
                }
            } finally {
                loadingState.update {
                    it.copy(loadingWatchlist = false)
                }
            }
        }
    }

    private fun loadPersonalListsData() {
        Timber.d("Loading personal lists data")
        viewModelScope.launch {
            try {
                loadingState.update {
                    it.copy(loadingPersonal = true)
                }
                val lists = getPersonalUseCase.getLists()
                listsPersonalState.update { lists }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e("Error loading personal lists: ${error.message}")
                }
            } finally {
                loadingState.update {
                    it.copy(loadingPersonal = false)
                }
            }
        }
    }

    fun loadLikedListsData(reload: Boolean = false) {
        Timber.d("Loading liked lists data")
        viewModelScope.launch {
            try {
                loadingState.update {
                    it.copy(loadingLiked = !reload)
                }
                val lists = getLikedUseCase.getLists()
                listsLikedState.update { lists }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!reload) {
                        errorState.update { error }
                    }
                    Timber.e("Error loading liked lists: ${error.message}")
                }
            } finally {
                marker = cacheMarkerProvider.getMarker()
                loadingState.update {
                    it.copy(loadingLiked = false)
                }
            }
        }
    }

    fun updateShowsData() {
        Timber.d("updateShowsData() called")
        viewModelScope.launch {
            try {
                if (showsLoadedAt == null) {
                    return@launch
                }

                val localUpdatedAt = showsLocalSyncSource.getWatchlistUpdatedAt()
                if (localUpdatedAt?.isAfter(showsLoadedAt) == true) {
                    loadWatchlistData(showLoading = false)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error")
                }
            }
        }
    }

    fun updateMoviesData() {
        Timber.d("updateMoviesData() called")
        viewModelScope.launch {
            try {
                if (moviesLoadedAt == null) {
                    return@launch
                }

                val localUpdatedAt = moviesLocalSyncSource.getWatchlistUpdatedAt()
                if (localUpdatedAt?.isAfter(moviesLoadedAt) == true) {
                    loadWatchlistData(showLoading = false)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error")
                }
            }
        }
    }

    fun updateLikedListsData() {
        if (marker == null) return
        viewModelScope.launch {
            if (marker != cacheMarkerProvider.getMarker()) {
                loadLikedListsData(reload = true)
            }
        }
    }

    val state = combine(
        showsState,
        moviesState,
        listsPersonalState,
        listsLikedState,
        loadingState,
        errorState,
    ) { s ->
        ListsState(
            watchlistShows = s[0] as ImmutableList<Show>?,
            watchlistMovies = s[1] as ImmutableList<Movie>?,
            personalLists = s[2] as ImmutableList<CustomList>?,
            likedLists = s[3] as ImmutableList<CustomList>?,
            loadingLists = s[4] as ListsState.LoadingState,
            error = s[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
