package tv.trakt.trakt.tv.core.home.sections.movies.availablenow

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.tv.core.home.sections.movies.availablenow.usecases.GetAvailableNowMoviesUseCase
import tv.trakt.trakt.tv.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.tv.helpers.extensions.nowUtc
import tv.trakt.trakt.tv.helpers.extensions.rethrowCancellation
import java.time.ZonedDateTime

internal class HomeAvailableNowViewModel(
    private val getAvailableNowUseCase: GetAvailableNowMoviesUseCase,
    private val localSyncSource: MoviesSyncLocalDataSource,
) : ViewModel() {
    private val initialState = HomeAvailableNowState()

    private val moviesState = MutableStateFlow(initialState.movies)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null

    init {
        loadData()
    }

    private fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
                }

                val movies = getAvailableNowUseCase.getMovies()
                moviesState.update { movies }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("HomeAvailableNowViewModel", "Error loading available now movies", error)
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun updateData() {
        Log.d("HomeAvailableNowViewModel", "updateData called")
        viewModelScope.launch {
            try {
                val localUpdatedAt = localSyncSource.getWatchlistUpdatedAt()
                if (localUpdatedAt != null && loadedAt?.isBefore(localUpdatedAt) == true) {
                    loadData(showLoading = false)
                    Log.d("HomeAvailableNowViewModel", "Updating available now movies")
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("HomeAvailableNowViewModel", "Error", error)
                }
            }
        }
    }

    val state: StateFlow<HomeAvailableNowState> = combine(
        moviesState,
        loadingState,
        errorState,
    ) { s1, s2, s3 ->
        HomeAvailableNowState(
            movies = s1,
            isLoading = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
