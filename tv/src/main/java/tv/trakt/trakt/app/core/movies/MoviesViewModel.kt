package tv.trakt.trakt.app.core.movies

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
import tv.trakt.trakt.app.core.movies.model.AnticipatedMovie
import tv.trakt.trakt.app.core.movies.model.TrendingMovie
import tv.trakt.trakt.app.core.movies.usecase.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetPopularMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetTrendingMoviesUseCase
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.User
import java.time.ZonedDateTime

@Suppress("UNCHECKED_CAST")
internal class MoviesViewModel(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val getAnticipatedMoviesUseCase: GetAnticipatedMoviesUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val sessionManager: SessionManager,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val initialState = MoviesState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val trendingMoviesState = MutableStateFlow(initialState.trendingMovies)
    private val popularMoviesState = MutableStateFlow(initialState.popularMovies)
    private val anticipatedMoviesState = MutableStateFlow(initialState.anticipatedMovies)
    private val recommendedMoviesState = MutableStateFlow(initialState.recommendedMovies)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null

    init {
        loadData()

        observeData()
        observeApp()
    }

    private fun observeApp() {
        appLifecycleProvider.observeState(FOREGROUND)
            .filter {
                loadedAt != null &&
                    nowUtc().minusMinutes(REFRESH_DATA_THRESHOLD_MINUTES).isAfter(loadedAt)
            }
            .onEach {
                loadData(showLoading = false)
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
                }
                userState.update { sessionManager.getProfile() }

                coroutineScope {
                    val trendingMoviesAsync = async { getTrendingMoviesUseCase.getTrendingMovies(10) }
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
                    val popularMovies = popularMoviesAsync.await()
                    val anticipatedMovies = anticipatedMoviesAsync.await()
                    val recommendedMovies = recommendedMoviesAsync.await()

                    trendingMoviesState.value = trendingMovies
                    popularMoviesState.value = popularMovies
                    anticipatedMoviesState.value = anticipatedMovies
                    recommendedMoviesState.value = recommendedMovies
                }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, error.message ?: "Unknown error")
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        trendingMoviesState,
        popularMoviesState,
        anticipatedMoviesState,
        recommendedMoviesState,
        userState,
        collectionStateProvider.stateFlow,
        errorState,
    ) { state ->
        MoviesState(
            isLoading = state[0] as Boolean,
            trendingMovies = state[1] as ImmutableList<TrendingMovie>?,
            popularMovies = state[2] as ImmutableList<Movie>?,
            anticipatedMovies = state[3] as ImmutableList<AnticipatedMovie>?,
            recommendedMovies = state[4] as ImmutableList<Movie>?,
            user = state[5] as User?,
            collection = state[6] as UserCollectionState,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
