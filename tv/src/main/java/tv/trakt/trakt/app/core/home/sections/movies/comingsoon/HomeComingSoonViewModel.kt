package tv.trakt.trakt.app.core.home.sections.movies.comingsoon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.home.sections.movies.comingsoon.usecases.GetComingSoonMoviesUseCase
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import java.time.ZonedDateTime

internal class HomeComingSoonViewModel(
    private val getComingSoonUseCase: GetComingSoonMoviesUseCase,
    private val localSyncSource: MoviesSyncLocalDataSource,
) : ViewModel() {
    private val initialState = HomeComingSoonState()

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

                val movies = getComingSoonUseCase.getMovies()
                moviesState.update { movies }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Error loading coming soon movies")
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun updateData() {
        Timber.d("updateData called")
        viewModelScope.launch {
            try {
                val localUpdatedAt = localSyncSource.getWatchlistUpdatedAt()
                if (localUpdatedAt != null && loadedAt?.isBefore(localUpdatedAt) == true) {
                    loadData(showLoading = false)
                    Timber.d("Updating coming soon movies")
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Error")
                }
            }
        }
    }

    val state: StateFlow<HomeComingSoonState> = combine(
        loadingState,
        moviesState,
        errorState,
    ) { isLoading, movies, error ->
        HomeComingSoonState(
            isLoading = isLoading,
            movies = movies,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
