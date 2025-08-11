package tv.trakt.trakt.app.core.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.core.movies.usecase.GetTrendingMoviesUseCase
import tv.trakt.trakt.app.core.search.SearchState.SearchResult
import tv.trakt.trakt.app.core.search.SearchState.State
import tv.trakt.trakt.app.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.app.core.search.usecase.recents.AddRecentSearchUseCase
import tv.trakt.trakt.app.core.search.usecase.recents.GetRecentSearchUseCase
import tv.trakt.trakt.app.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.app.core.shows.model.Show
import tv.trakt.trakt.app.core.shows.usecase.GetTrendingShowsUseCase
import tv.trakt.trakt.app.helpers.extensions.asyncMap
import tv.trakt.trakt.app.helpers.extensions.rethrowCancellation

internal class SearchViewModel(
    private val getSearchResultsUseCase: GetSearchResultsUseCase,
    private val addRecentSearchUseCase: AddRecentSearchUseCase,
    private val getRecentSearchUseCase: GetRecentSearchUseCase,
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val showLocalSource: ShowLocalDataSource,
    private val movieLocalSource: MovieLocalDataSource,
) : ViewModel() {
    private val initialState = SearchState()

    private val screenState = MutableStateFlow(initialState.state)
    private val trendingResultState = MutableStateFlow(initialState.trendingResult)
    private val recentsResultState = MutableStateFlow(initialState.recentsResult)
    private val searchResultState = MutableStateFlow(initialState.searchResult)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val searchingState = MutableStateFlow(initialState.searching)
    private val errorState = MutableStateFlow(initialState.error)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)

    private var searchJob: Job? = null

    init {
        loadBackground()
        loadRecentlySearched()
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString("background_image_url")
        backgroundState.update { configUrl }
    }

    private fun loadRecentlySearched() {
        viewModelScope.launch {
            try {
                val recentShowsAsync = async { getRecentSearchUseCase.getRecentShows() }
                val recentMoviesAsync = async { getRecentSearchUseCase.getRecentMovies() }

                val recentShows = recentShowsAsync.await()
                val recentMovies = recentMoviesAsync.await()

                if (recentShows.isEmpty() && recentMovies.isEmpty()) {
                    loadTrendingSearches()
                    return@launch
                }

                if (searchingState.value || screenState.value == State.SEARCH_RESULTS) {
                    return@launch
                }

                recentsResultState.update {
                    SearchResult(
                        shows = recentShows,
                        movies = recentMovies,
                    )
                }

                screenState.update { State.RECENTS }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.w("SearchViewModel", "Error loading recent searches!", error)
                }
            }
        }
    }

    private fun loadTrendingSearches() {
        viewModelScope.launch {
            try {
                val showsAsync = async { getTrendingShowsUseCase.getTrendingShows(10) }
                val moviesAsync = async { getTrendingMoviesUseCase.getTrendingMovies(10) }

                val shows = showsAsync.await()
                val movies = moviesAsync.await()

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
                    Log.w("SearchViewModel", "Error loading trending searches!", error)
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

                delay(500) // Throttle user input
                getSearchResultsUseCase.getSearchResults(query).run {
                    searchResultState.update {
                        SearchResult(
                            shows = shows,
                            movies = movies,
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
                    Log.e("SearchViewModel", "Error!", error)
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
            addRecentSearchUseCase.addRecentSearchShow(show)
            navigateShow.update { show }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateShow.value != null || navigateMovie.value != null) {
            return
        }
        viewModelScope.launch {
            movieLocalSource.upsertMovies(listOf(movie))
            addRecentSearchUseCase.addRecentSearchMovie(movie)
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

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<SearchState> = combine(
        screenState,
        searchResultState,
        recentsResultState,
        trendingResultState,
        navigateShow,
        navigateMovie,
        backgroundState,
        searchingState,
        errorState,
    ) { state ->
        SearchState(
            state = state[0] as State,
            searchResult = state[1] as SearchResult?,
            recentsResult = state[2] as SearchResult?,
            trendingResult = state[3] as SearchResult?,
            navigateShow = state[4] as Show?,
            navigateMovie = state[5] as Movie?,
            backgroundUrl = state[6] as String?,
            searching = state[7] as Boolean,
            error = state[8] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
