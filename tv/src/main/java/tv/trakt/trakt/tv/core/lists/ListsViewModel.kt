package tv.trakt.trakt.tv.core.lists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.tv.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.tv.core.lists.usecases.GetListsMoviesWatchlistUseCase
import tv.trakt.trakt.tv.core.lists.usecases.GetListsShowsWatchlistUseCase
import tv.trakt.trakt.tv.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.tv.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.tv.helpers.extensions.nowUtc
import java.time.ZonedDateTime

internal class ListsViewModel(
    private val getShowsWatchlistUseCase: GetListsShowsWatchlistUseCase,
    private val getMoviesWatchlistUseCase: GetListsMoviesWatchlistUseCase,
    private val showsLocalSyncSource: ShowsSyncLocalDataSource,
    private val moviesLocalSyncSource: MoviesSyncLocalDataSource,
) : ViewModel() {
    private val initialState = ListsState()

    private val showsState = MutableStateFlow(initialState.watchlistShows)
    private val moviesState = MutableStateFlow(initialState.watchlistMovies)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var showsLoadedAt: ZonedDateTime? = null
    private var moviesLoadedAt: ZonedDateTime? = null

    init {
        loadData()
    }

    private fun loadData(showLoading: Boolean = true) {
        Log.d("ListsViewModel", "Loading data")
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
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
                    Log.e("ListsViewModel", "Error loading: ${error.message}")
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun updateShowsData() {
        Log.d("ListsViewModel", "updateShowsData() called")
        viewModelScope.launch {
            try {
                if (showsLoadedAt == null) {
                    return@launch
                }

                val localUpdatedAt = showsLocalSyncSource.getWatchlistUpdatedAt()
                if (localUpdatedAt?.isAfter(showsLoadedAt) == true) {
                    loadData(showLoading = false)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("ListsViewModel", "Error", error)
                }
            }
        }
    }

    fun updateMoviesData() {
        Log.d("ListsViewModel", "updateMoviesData() called")
        viewModelScope.launch {
            try {
                if (moviesLoadedAt == null) {
                    return@launch
                }

                val localUpdatedAt = moviesLocalSyncSource.getWatchlistUpdatedAt()
                if (localUpdatedAt?.isAfter(moviesLoadedAt) == true) {
                    loadData(showLoading = false)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("ListsViewModel", "Error", error)
                }
            }
        }
    }

    val state: StateFlow<ListsState> = combine(
        showsState,
        moviesState,
        loadingState,
        errorState,
    ) { s1, s2, s3, s4 ->
        ListsState(
            watchlistShows = s1,
            watchlistMovies = s2,
            isLoading = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
