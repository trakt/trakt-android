package tv.trakt.trakt.app.core.lists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.common.model.CustomList
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.app.core.lists.usecases.GetListsMoviesWatchlistUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsPersonalUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsShowsWatchlistUseCase
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import java.time.ZonedDateTime

internal class ListsViewModel(
    private val getShowsWatchlistUseCase: GetListsShowsWatchlistUseCase,
    private val getMoviesWatchlistUseCase: GetListsMoviesWatchlistUseCase,
    private val getPersonalUseCase: GetListsPersonalUseCase,
    private val showsLocalSyncSource: ShowsSyncLocalDataSource,
    private val moviesLocalSyncSource: MoviesSyncLocalDataSource,
) : ViewModel() {
    private val initialState = ListsState()

    private val showsState = MutableStateFlow(initialState.watchlistShows)
    private val moviesState = MutableStateFlow(initialState.watchlistMovies)
    private val listsState = MutableStateFlow(initialState.personalLists)
    private val loadingWatchlistState = MutableStateFlow(initialState.isLoadingWatchlist)
    private val loadingPersonalState = MutableStateFlow(initialState.isLoadingWatchlist)
    private val errorState = MutableStateFlow(initialState.error)

    private var showsLoadedAt: ZonedDateTime? = null
    private var moviesLoadedAt: ZonedDateTime? = null

    init {
        loadWatchlistData()
        loadPersonalListsData()
    }

    private fun loadWatchlistData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingWatchlistState.update { true }
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
                    errorState.value = error
                    Timber.e(error, "Error loading: ${error.message}")
                }
            } finally {
                loadingWatchlistState.update { false }
            }
        }
    }

    private fun loadPersonalListsData() {
        Log.d("ListsViewModel", "Loading personal lists data")
        viewModelScope.launch {
            try {
                loadingPersonalState.update { true }
                val lists = getPersonalUseCase.getLists()
                listsState.update { lists }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.value = error
                    Log.e("ListsViewModel", "Error loading personal lists: ${error.message}")
                }
            } finally {
                loadingPersonalState.update { false }
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

    val state: StateFlow<ListsState> = combine(
        showsState,
        moviesState,
        listsState,
        loadingWatchlistState,
        loadingPersonalState,
        errorState,
    ) { s ->
        @Suppress("UNCHECKED_CAST")
        ListsState(
            watchlistShows = s[0] as ImmutableList<Show>?,
            watchlistMovies = s[1] as ImmutableList<Movie>?,
            personalLists = s[2] as ImmutableList<CustomList>?,
            isLoadingWatchlist = s[3] as Boolean,
            isLoadingPersonal = s[4] as Boolean,
            error = s[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
