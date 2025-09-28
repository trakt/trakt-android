package tv.trakt.trakt.core.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.movies.sections.trending.usecase.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.search.SearchState.SearchResult
import tv.trakt.trakt.core.search.SearchState.State
import tv.trakt.trakt.core.search.SearchState.UserState
import tv.trakt.trakt.core.search.model.SearchFilter.MEDIA
import tv.trakt.trakt.core.search.model.SearchFilter.MOVIES
import tv.trakt.trakt.core.search.model.SearchFilter.PEOPLE
import tv.trakt.trakt.core.search.model.SearchFilter.SHOWS
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.core.search.usecase.GetBirthdayPeopleUseCase
import tv.trakt.trakt.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.core.search.usecase.recents.AddRecentSearchUseCase
import tv.trakt.trakt.core.search.usecase.recents.GetRecentSearchUseCase
import tv.trakt.trakt.core.shows.sections.trending.usecase.GetTrendingShowsUseCase

@OptIn(FlowPreview::class)
internal class SearchViewModel(
    private val getSearchResultsUseCase: GetSearchResultsUseCase,
    private val addRecentSearchUseCase: AddRecentSearchUseCase,
    private val getRecentSearchUseCase: GetRecentSearchUseCase,
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getBirthdayPeopleUseCase: GetBirthdayPeopleUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = SearchState()

    private val inputState = MutableStateFlow(initialState.input)
    private val screenState = MutableStateFlow(initialState.state)
    private val popularResultState = MutableStateFlow(initialState.popularResults)
    private val recentsResultState = MutableStateFlow(initialState.recentsResult)
    private val searchResultState = MutableStateFlow(initialState.searchResult)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val searchingState = MutableStateFlow(initialState.searching)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    private var initialJob: Job? = null
    private var searchJob: Job? = null

    init {
        observeUser()
        loadBackground()

        initialJob = viewModelScope.launch {
            try {
                loadRecentlySearched()
                loadPopularSearches()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    searchingState.update { false }
                    Timber.w(error, "Error during initial load!")
                }
            }
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.observeProfile()
                .distinctUntilChanged()
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
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private suspend fun loadRecentlySearched() {
        return coroutineScope {
            val recentShowsAsync = async { getRecentSearchUseCase.getRecentShows() }
            val recentMoviesAsync = async { getRecentSearchUseCase.getRecentMovies() }
            val recentPeopleAsync = async { getRecentSearchUseCase.getRecentPeople() }

            val recentShows = when (inputState.value.filter) {
                in arrayOf(MEDIA, SHOWS) -> recentShowsAsync.await()
                else -> emptyList()
            }
            val recentMovies = when (inputState.value.filter) {
                in arrayOf(MEDIA, MOVIES) -> recentMoviesAsync.await()
                else -> emptyList()
            }
            val recentPeople = when (inputState.value.filter) {
                in arrayOf(PEOPLE) -> recentPeopleAsync.await()
                else -> emptyList()
            }

            if (searchingState.value || screenState.value == State.SEARCH_RESULTS) {
                return@coroutineScope
            }

            recentsResultState.update {
                SearchResult(
                    items = buildList {
                        val showItems = recentShows.asyncMap {
                            SearchItem.Show(
                                rank = it.createdAt.toInstant().toEpochMilli(),
                                show = it.show,
                            )
                        }
                        val movieItems = recentMovies.asyncMap {
                            SearchItem.Movie(
                                rank = it.createdAt.toInstant().toEpochMilli(),
                                movie = it.movie,
                            )
                        }
                        val peopleItems = recentPeople.asyncMap {
                            SearchItem.Person(
                                rank = it.createdAt.toInstant().toEpochMilli(),
                                person = it.person,
                            )
                        }

                        addAll(showItems)
                        addAll(movieItems)
                        addAll(peopleItems)
                    }
                        .sortedByDescending { it.rank }
                        .take(3)
                        .toImmutableList(),
                )
            }
        }
    }

    private suspend fun loadPopularSearches() {
        return coroutineScope {
            if (searchingState.value || screenState.value == State.SEARCH_RESULTS) {
                return@coroutineScope
            }

            val showsAsync = async {
                getTrendingShowsUseCase.getLocalShows().ifEmpty {
                    getTrendingShowsUseCase.getShows()
                }
            }
            val moviesAsync = async {
                getTrendingMoviesUseCase.getLocalMovies().ifEmpty {
                    getTrendingMoviesUseCase.getMovies()
                }
            }
            val peopleAsync = async {
                var localPeople = getBirthdayPeopleUseCase.getLocalPeople()
                if (localPeople.firstOrNull()?.birthday?.monthValue != nowUtc().monthValue) {
                    // If the month of the first person's birthday is not the current month, local data is outdated.
                    // Clear it to fetch fresh.
                    localPeople = emptyList<Person>().toImmutableList()
                }
                localPeople.ifEmpty {
                    getBirthdayPeopleUseCase.getPeople()
                }
            }

            val filter = inputState.value.filter
            val shows = when (filter) {
                in arrayOf(MEDIA, SHOWS) -> showsAsync.await()
                else -> emptyList()
            }
            val movies = when (filter) {
                in arrayOf(MEDIA, MOVIES) -> moviesAsync.await()
                else -> emptyList()
            }
            val people = when (filter) {
                in arrayOf(PEOPLE) -> peopleAsync.await()
                else -> emptyList()
            }

            val results = when (filter) {
                PEOPLE -> {
                    SearchResult(
                        items = people
                            .asyncMap {
                                SearchItem.Person(
                                    rank = 0,
                                    person = it,
                                    showBirthday = true,
                                )
                            }
                            .toImmutableList(),
                    )
                }
                else -> {
                    SearchResult(
                        items = buildList {
                            val showItems = shows.asyncMap {
                                SearchItem.Show(it.watchers.toLong(), it.show)
                            }
                            val movieItems = movies.asyncMap {
                                SearchItem.Movie(it.watchers.toLong(), it.movie)
                            }

                            // Interleave shows and movies, taking one from each list at a time
                            val maxSize = maxOf(showItems.size, movieItems.size)
                            for (i in 0 until maxSize) {
                                if (i < showItems.size) {
                                    add(showItems[i])
                                }
                                if (i < movieItems.size) {
                                    add(movieItems[i])
                                }
                            }
                        }
                            .take(if (shows.isEmpty() || movies.isEmpty()) 18 else 36)
                            .toImmutableList(),
                    )
                }
            }

            popularResultState.update { results }
        }
    }

    fun updateSearch(searchInput: SearchInput) {
        val currentFilter = inputState.value.filter
        val currentQuery = inputState.value.query
        inputState.update { searchInput }

        if (currentFilter != searchInput.filter) {
            clearJobs()

            if (searchInput.query.isNotBlank()) {
                onSearchQuery(searchInput.query, 0)
            } else {
                initialJob = viewModelScope.launch {
                    try {
                        loadRecentlySearched()
                        loadPopularSearches()
                    } catch (error: Exception) {
                        error.rethrowCancellation {
                            errorState.update { error }
                            Timber.w(error, "Error during initial load!")
                        }
                    } finally {
                        searchingState.update { false }
                    }
                }
            }
        }

        if (currentQuery != searchInput.query) {
            clearJobs()
            onSearchQuery(searchInput.query)
        }
    }

    private fun onSearchQuery(
        query: String,
        debounce: Long = 350,
    ) {
        if (query.isBlank()) {
            searchingState.update { false }
            searchResultState.update { null }
            return
        }

        searchJob = viewModelScope.launch {
            try {
                searchingState.update { true }
                delay(debounce) // Debounce user input

                val results = when (inputState.value.filter) {
                    MEDIA -> getSearchResultsUseCase.getSearchResults(query)
                    SHOWS -> getSearchResultsUseCase.getShowsSearchResults(query)
                    MOVIES -> getSearchResultsUseCase.getMoviesSearchResults(query)
                    PEOPLE -> getSearchResultsUseCase.getPeopleSearchResults(query)
                }

                searchResultState.update {
                    SearchResult(
                        items = results
                            .map {
                                when {
                                    it.show != null -> SearchItem.Show(
                                        rank = it.score,
                                        show = Show.fromDto(it.show!!),
                                    )
                                    it.movie != null -> SearchItem.Movie(
                                        rank = it.score,
                                        movie = Movie.fromDto(it.movie!!),
                                    )
                                    it.person != null -> SearchItem.Person(
                                        rank = it.score,
                                        person = Person.fromDto(it.person!!),
                                    )
                                    else -> throw Error("Unexpected null show and movie")
                                }
                            }.toImmutableList(),
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error!")
                }
            } finally {
                searchingState.update { false }
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

    fun navigateToPerson(person: Person) {
        if (navigateShow.value != null || navigateMovie.value != null) {
            return
        }
        viewModelScope.launch {
            // Note: In app module, we don't have movieLocalSource
            // This functionality might need to be adapted based on app module architecture
            addRecentSearchUseCase.addRecentSearchPerson(person)
//            navigateMovie.update { movie }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    private fun clearJobs() {
        searchJob?.cancel()
        searchJob = null

        initialJob?.cancel()
        initialJob = null
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<SearchState> = combine(
        screenState,
        inputState,
        searchResultState,
        recentsResultState,
        popularResultState,
        navigateShow,
        navigateMovie,
        backgroundState,
        searchingState,
        userState,
        errorState,
    ) { state ->
        SearchState(
            state = state[0] as State,
            input = state[1] as SearchInput,
            searchResult = state[2] as SearchResult?,
            recentsResult = state[3] as SearchResult?,
            popularResults = state[4] as SearchResult?,
            navigateShow = state[5] as Show?,
            navigateMovie = state[6] as Movie?,
            backgroundUrl = state[7] as String?,
            searching = state[8] as Boolean,
            user = state[9] as UserState,
            error = state[10] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
