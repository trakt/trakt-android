package tv.trakt.trakt.app.core.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.movies.usecase.GetTrendingMoviesUseCase
import tv.trakt.trakt.app.core.search.SearchState.SearchResult
import tv.trakt.trakt.app.core.search.SearchState.State
import tv.trakt.trakt.app.core.shows.usecase.GetTrendingShowsUseCase
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import kotlin.time.Duration.Companion.milliseconds

internal class SearchViewModel(
    private val getSearchResultsUseCase: GetSearchResultsUseCase,
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val showLocalSource: ShowLocalDataSource,
    private val movieLocalSource: MovieLocalDataSource,
    private val collectionProvider: CollectionStateProvider,
) : ViewModel() {
    private val initialState = SearchState()

    private val screenState = MutableStateFlow(initialState.state)
    private val trendingResultState = MutableStateFlow(initialState.trendingResult)
    private val searchResultState = MutableStateFlow(initialState.searchResult)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val searchingState = MutableStateFlow(initialState.searching)
    private val errorState = MutableStateFlow(initialState.error)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)

    private var searchJob: Job? = null

    init {
        loadBackground()
        loadTrendingSearches()

        observeData()
    }

    private fun observeData() {
        collectionProvider
            .launchIn(viewModelScope)
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private fun loadTrendingSearches() {
        viewModelScope.launch {
            try {
                val (shows, movies) = coroutineScope {
                    val showsAsync = async { getTrendingShowsUseCase.getTrendingShows(20) }
                    val moviesAsync = async { getTrendingMoviesUseCase.getTrendingMovies(20) }

                    val shows = showsAsync.await()
                    val movies = moviesAsync.await()

                    return@coroutineScope (shows to movies)
                }

                if (searchingState.value || screenState.value == State.SEARCH_RESULTS) {
                    return@launch
                }

                trendingResultState.update {
                    SearchResult(
                        shows = (shows.asyncMap { it.show }).toImmutableList(),
                        movies = (movies.asyncMap { it.movie }).toImmutableList(),
                    )
                }

                screenState.update { State.TRENDING }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error loading trending searches!")
                }
            }
        }
    }

    fun searchQuery(query: String) {
        clearSearchJob()
        if (query.isBlank()) {
            searchingState.update { false }
            return
        }

        searchJob = viewModelScope.launch {
            try {
                searchingState.update { true }

                delay(500.milliseconds) // Throttle user input
                getSearchResultsUseCase.getSearchResults(query).run {
                    searchResultState.update {
                        SearchResult(
                            shows = mapNotNull { dto -> dto.show?.let { Show.fromDto(it) } }
                                .toImmutableList(),
                            movies = mapNotNull { dto -> dto.movie?.let { Movie.fromDto(it) } }
                                .toImmutableList(),
                        )
                    }
                }

                searchingState.update { false }
                screenState.update { State.SEARCH_RESULTS }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.value = error
                    searchingState.update { false }
                    screenState.update { State.ERROR }
                    Timber.e(error, "Error!")
                }
            }
        }
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || navigateMovie.value != null) {
            return
        }
        viewModelScope.launch {
            showLocalSource.upsertShows(listOf(show))
            navigateShow.update { show }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateShow.value != null || navigateMovie.value != null) {
            return
        }
        viewModelScope.launch {
            movieLocalSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    private fun clearSearchJob() {
        searchJob?.cancel()
        searchJob = null
    }

    val state = combine(
        screenState,
        searchResultState,
        trendingResultState,
        navigateShow,
        navigateMovie,
        backgroundState,
        searchingState,
        collectionProvider.stateFlow,
        errorState,
    ) { state ->
        SearchState(
            state = state[0] as State,
            searchResult = state[1] as SearchResult?,
            trendingResult = state[2] as SearchResult?,
            navigateShow = state[3] as Show?,
            navigateMovie = state[4] as Movie?,
            backgroundUrl = state[5] as String?,
            searching = state[6] as Boolean,
            collection = state[7] as UserCollectionState,
            error = state[8] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
