package tv.trakt.app.tv.core.movies

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
import tv.trakt.app.tv.auth.session.SessionManager
import tv.trakt.app.tv.core.movies.model.AnticipatedMovie
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.core.movies.model.TrendingMovie
import tv.trakt.app.tv.core.movies.usecase.GetAnticipatedMoviesUseCase
import tv.trakt.app.tv.core.movies.usecase.GetHotMoviesUseCase
import tv.trakt.app.tv.core.movies.usecase.GetPopularMoviesUseCase
import tv.trakt.app.tv.core.movies.usecase.GetRecommendedMoviesUseCase
import tv.trakt.app.tv.core.movies.usecase.GetTrendingMoviesUseCase
import tv.trakt.app.tv.helpers.extensions.rethrowCancellation

internal class MoviesViewModel(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val getAnticipatedMoviesUseCase: GetAnticipatedMoviesUseCase,
    private val getHotMoviesUseCase: GetHotMoviesUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = MoviesState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val trendingMoviesState = MutableStateFlow(initialState.trendingMovies)
    private val hotMoviesState = MutableStateFlow(initialState.hotMovies)
    private val popularMoviesState = MutableStateFlow(initialState.popularMovies)
    private val anticipatedMoviesState = MutableStateFlow(initialState.anticipatedMovies)
    private val recommendedMoviesState = MutableStateFlow(initialState.recommendedMovies)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }
                coroutineScope {
                    val trendingMoviesAsync = async { getTrendingMoviesUseCase.getTrendingMovies() }
                    val hotMoviesAsync = async { getHotMoviesUseCase.getHotMovies() }
                    val popularMoviesAsync = async { getPopularMoviesUseCase.getPopularMovies() }
                    val anticipatedMoviesAsync = async { getAnticipatedMoviesUseCase.getAnticipatedMovies() }

                    val recommendedMoviesAsync = async {
                        if (sessionManager.isAuthenticated()) {
                            getRecommendedMoviesUseCase.getRecommendedMovies()
                        } else {
                            null
                        }
                    }

                    val trendingMovies = trendingMoviesAsync.await()
                    val hotMovies = hotMoviesAsync.await()
                    val popularMovies = popularMoviesAsync.await()
                    val anticipatedMovies = anticipatedMoviesAsync.await()
                    val recommendedMovies = recommendedMoviesAsync.await()

                    trendingMoviesState.value = trendingMovies
                    hotMoviesState.value = hotMovies
                    popularMoviesState.value = popularMovies
                    anticipatedMoviesState.value = anticipatedMovies
                    recommendedMoviesState.value = recommendedMovies
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Log.e("MoviesViewModel", error.message ?: "Unknown error")
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state: StateFlow<MoviesState> = combine(
        loadingState,
        trendingMoviesState,
        hotMoviesState,
        popularMoviesState,
        anticipatedMoviesState,
        recommendedMoviesState,
        errorState,
    ) { s ->
        @Suppress("UNCHECKED_CAST")
        MoviesState(
            isLoading = s[0] as Boolean,
            trendingMovies = s[1] as ImmutableList<TrendingMovie>?,
            hotMovies = s[2] as ImmutableList<TrendingMovie>?,
            popularMovies = s[3] as ImmutableList<Movie>?,
            anticipatedMovies = s[4] as ImmutableList<AnticipatedMovie>?,
            recommendedMovies = s[5] as ImmutableList<Movie>?,
            error = s[6] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
