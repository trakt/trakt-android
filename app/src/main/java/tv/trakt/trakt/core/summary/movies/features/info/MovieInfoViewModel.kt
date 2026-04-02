@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.summary.movies.features.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.networking.MovieStatsDto
import tv.trakt.trakt.core.summary.movies.features.info.usecase.GetMovieStatsUseCase
import tv.trakt.trakt.core.summary.movies.features.info.usecase.GetMovieStudiosUseCase

internal class MovieInfoViewModel(
    private val movie: Movie,
    private val getStatsUseCase: GetMovieStatsUseCase,
    private val getStudiosUseCase: GetMovieStudiosUseCase,
) : ViewModel() {
    private val initialState = MovieInfoState()

    private val movieState = MutableStateFlow(movie)
    private val movieStatsState = MutableStateFlow(initialState.movieStats)
    private val movieStudiosState = MutableStateFlow(initialState.movieStudios)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                coroutineScope {
                    awaitAll(
                        async { loadStats() },
                        async { loadStudios() },
                    )
                }

                loadingState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            }
        }
    }

    private suspend fun loadStudios() {
        try {
            movieStudiosState.update {
                getStudiosUseCase.getStudios(movie.ids.trakt)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    private suspend fun loadStats() {
        try {
            movieStatsState.update {
                getStatsUseCase.getStats(movie.ids.trakt)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    val state = combine(
        movieState,
        movieStatsState,
        movieStudiosState,
        loadingState,
        errorState,
    ) { state ->
        MovieInfoState(
            movie = state[0] as Movie?,
            movieStats = state[1] as MovieStatsDto?,
            movieStudios = state[2] as ImmutableList<String>?,
            loading = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
