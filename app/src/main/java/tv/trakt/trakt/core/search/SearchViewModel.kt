package tv.trakt.trakt.core.search

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
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.movies.sections.trending.usecase.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.search.SearchState.SearchResult
import tv.trakt.trakt.core.search.SearchState.State
import tv.trakt.trakt.core.search.SearchState.UserState
import tv.trakt.trakt.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.core.search.usecase.recents.AddRecentSearchUseCase
import tv.trakt.trakt.core.search.usecase.recents.GetRecentSearchUseCase
import tv.trakt.trakt.core.shows.sections.trending.usecase.GetTrendingShowsUseCase

internal class SearchViewModel(
    private val getSearchResultsUseCase: GetSearchResultsUseCase,
    private val addRecentSearchUseCase: AddRecentSearchUseCase,
    private val getRecentSearchUseCase: GetRecentSearchUseCase,
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = SearchState()

    private val screenState = MutableStateFlow(initialState.state)
    private val trendingResultState = MutableStateFlow(initialState.trendingResult)
    private val recentsResultState = MutableStateFlow(initialState.recentsResult)
    private val searchResultState = MutableStateFlow(initialState.searchResult)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val searchingState = MutableStateFlow(initialState.searching)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    private var searchJob: Job? = null

    init {
        loadBackground()
        loadRecentlySearched()
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.observeProfile()
                .collect { user ->
                    userState.update {
                        UserState(
                            user = user,
                            loading = LoadingState.DONE,
                        )
                    }
                }
        }
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString("mobile_background_image_url")
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
                    Timber.w(error, "Error loading recent searches!")
                }
            }
        }
    }

    private fun loadTrendingSearches() {
        viewModelScope.launch {
            try {
                val showsAsync = async { getTrendingShowsUseCase.getShows(10) }
                val moviesAsync = async { getTrendingMoviesUseCase.getMovies(10) }

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
                    Timber.w(error, "Error loading trending searches!")
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
            // Note: In app module, we don't have showLocalSource
            // This functionality might need to be adapted based on app module architecture
            addRecentSearchUseCase.addRecentSearchShow(show)
            navigateShow.update { show }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateShow.value != null || navigateMovie.value != null) {
            return
        }
        viewModelScope.launch {
            // Note: In app module, we don't have movieLocalSource
            // This functionality might need to be adapted based on app module architecture
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
        userState,
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
            user = state[8] as UserState,
            error = state[9] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
