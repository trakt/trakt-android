package tv.trakt.trakt.core.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource
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
import tv.trakt.trakt.core.search.usecase.popular.GetPopularSearchUseCase
import tv.trakt.trakt.core.search.usecase.popular.PostUserSearchUseCase
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState

@OptIn(FlowPreview::class)
internal class SearchViewModel(
    private val getPopularSearchesUseCase: GetPopularSearchUseCase,
    private val postUserSearchUseCase: PostUserSearchUseCase,
    private val getSearchResultsUseCase: GetSearchResultsUseCase,
//    private val addRecentSearchUseCase: AddRecentSearchUseCase,
//    private val getRecentSearchUseCase: GetRecentSearchUseCase,
    private val getBirthdayPeopleUseCase: GetBirthdayPeopleUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val peopleLocalDataSource: PeopleLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = SearchState()

    private val inputState = MutableStateFlow(initialState.input)
    private val screenState = MutableStateFlow(initialState.state)
    private val popularResultState = MutableStateFlow(initialState.popularResults)

    //    private val recentsResultState = MutableStateFlow(initialState.recentsResult)
    private val searchResultState = MutableStateFlow(initialState.searchResult)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val navigatePerson = MutableStateFlow(initialState.navigatePerson)
    private val searchingState = MutableStateFlow(initialState.searching)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    private var initialJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadInitialData()

        observeUser()
        observeCollection()

        analytics.logScreenView(
            screenName = "search",
        )
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

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                loadLocalPopularSearches()
                loadPopularSearches()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    searchingState.update { false }
                    Timber.e(error, "Error during initial load!")
                }
            }
        }
    }

    private suspend fun loadLocalPopularSearches() {
        return coroutineScope {
            if (searchingState.value || screenState.value == State.SEARCH_RESULTS) {
                return@coroutineScope
            }

            val showsAsync = async {
                getPopularSearchesUseCase.getLocalShows()
            }
            val moviesAsync = async {
                getPopularSearchesUseCase.getLocalMovies()
            }
            val peopleAsync = async {
                var localPeople = getBirthdayPeopleUseCase.getLocalPeople()
                if (localPeople.firstOrNull()?.birthday?.monthValue != nowUtc().monthValue) {
                    // If the month of the first person's birthday is not the current month, local data is outdated.
                    // Clear it to fetch fresh.
                    localPeople = emptyList<Person>().toImmutableList()
                }
                localPeople
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
                                SearchItem.Show(
                                    rank = it.rank.toLong(),
                                    show = it.show,
                                )
                            }
                            val movieItems = movies.asyncMap {
                                SearchItem.Movie(
                                    rank = it.rank.toLong(),
                                    movie = it.movie,
                                )
                            }
                            addAll(showItems)
                            addAll(movieItems)
                        }
                            .sortedByDescending { it.rank }
                            .toImmutableList(),
                    )
                }
            }

            popularResultState.update { results }
        }
    }

    private suspend fun loadPopularSearches() {
        return coroutineScope {
            if (searchingState.value || screenState.value == State.SEARCH_RESULTS) {
                return@coroutineScope
            }

            val showsAsync = async {
                if (!getPopularSearchesUseCase.isLocalShowsValid()) {
                    getPopularSearchesUseCase.getShows()
                } else {
                    getPopularSearchesUseCase.getLocalShows()
                }
            }
            val moviesAsync = async {
                if (!getPopularSearchesUseCase.isLocalMoviesValid()) {
                    getPopularSearchesUseCase.getMovies()
                } else {
                    getPopularSearchesUseCase.getLocalMovies()
                }
            }
            val peopleAsync = async {
                if (!getBirthdayPeopleUseCase.isLocalPeopleValid()) {
                    getBirthdayPeopleUseCase.getPeople()
                } else {
                    getBirthdayPeopleUseCase.getLocalPeople()
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
                                SearchItem.Show(
                                    rank = it.rank.toLong(),
                                    show = it.show,
                                )
                            }
                            val movieItems = movies.asyncMap {
                                SearchItem.Movie(
                                    rank = it.rank.toLong(),
                                    movie = it.movie,
                                )
                            }
                            addAll(showItems)
                            addAll(movieItems)
                        }
                            .sortedByDescending { it.rank }
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
                        loadLocalPopularSearches()
                    } catch (error: Exception) {
                        error.rethrowCancellation {
                            errorState.update { error }
                            Timber.e(error, "Error during initial load!")
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
        if (navigateShow.value != null ||
            navigateMovie.value != null ||
            navigatePerson.value != null
        ) {
            return
        }

        viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show }
            postUserSearch(show)
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateShow.value != null ||
            navigateMovie.value != null ||
            navigatePerson.value != null
        ) {
            return
        }

        viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie }
            postUserSearch(movie)
        }
    }

    fun navigateToPerson(person: Person) {
        if (navigateShow.value != null ||
            navigateMovie.value != null ||
            navigatePerson.value != null
        ) {
            return
        }

        viewModelScope.launch {
            peopleLocalDataSource.upsertPeople(listOf(person))
            navigatePerson.update { person }
        }
    }

    private fun postUserSearch(show: Show) {
        viewModelScope.launch {
            try {
                if (inputState.value.query.isBlank() || !sessionManager.isAuthenticated()) {
                    return@launch
                }
                postUserSearchUseCase.postShowUserSearch(
                    showId = show.ids.trakt,
                    query = inputState.value.query,
                )
            } catch (error: Exception) {
                Timber.e(error, "Error posting user search")
            }
        }
    }

    private fun postUserSearch(movie: Movie) {
        viewModelScope.launch {
            try {
                if (inputState.value.query.isBlank() || !sessionManager.isAuthenticated()) {
                    return@launch
                }
                postUserSearchUseCase.postMovieUserSearch(
                    movieId = movie.ids.trakt,
                    query = inputState.value.query,
                )
            } catch (error: Exception) {
                Timber.e(error, "Error posting user search")
            }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
        navigatePerson.update { null }
    }

    private fun clearJobs() {
        searchJob?.cancel()
        initialJob?.cancel()

        searchJob = null
        initialJob = null
    }

//    Disabled until we decide how to do recently searched.
//    private suspend fun loadRecentlySearched() {
//        return coroutineScope {
//            val recentShowsAsync = async { getRecentSearchUseCase.getRecentShows() }
//            val recentMoviesAsync = async { getRecentSearchUseCase.getRecentMovies() }
//            val recentPeopleAsync = async { getRecentSearchUseCase.getRecentPeople() }
//
//            val recentShows = when (inputState.value.filter) {
//                in arrayOf(MEDIA, SHOWS) -> recentShowsAsync.await()
//                else -> emptyList()
//            }
//            val recentMovies = when (inputState.value.filter) {
//                in arrayOf(MEDIA, MOVIES) -> recentMoviesAsync.await()
//                else -> emptyList()
//            }
//            val recentPeople = when (inputState.value.filter) {
//                in arrayOf(PEOPLE) -> recentPeopleAsync.await()
//                else -> emptyList()
//            }
//
//            if (searchingState.value || screenState.value == State.SEARCH_RESULTS) {
//                return@coroutineScope
//            }
//
//            recentsResultState.update {
//                SearchResult(
//                    items = buildList {
//                        val showItems = recentShows.asyncMap {
//                            SearchItem.Show(
//                                rank = it.createdAt.toInstant().toEpochMilli(),
//                                show = it.show,
//                            )
//                        }
//                        val movieItems = recentMovies.asyncMap {
//                            SearchItem.Movie(
//                                rank = it.createdAt.toInstant().toEpochMilli(),
//                                movie = it.movie,
//                            )
//                        }
//                        val peopleItems = recentPeople.asyncMap {
//                            SearchItem.Person(
//                                rank = it.createdAt.toInstant().toEpochMilli(),
//                                person = it.person,
//                            )
//                        }
//
//                        addAll(showItems)
//                        addAll(movieItems)
//                        addAll(peopleItems)
//                    }
//                        .sortedByDescending { it.rank }
//                        .take(3)
//                        .toImmutableList(),
//                )
//            }
//        }
//    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        screenState,
        inputState,
        searchResultState,
        popularResultState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        navigatePerson,
        searchingState,
        userState,
        errorState,
    ) { state ->
        SearchState(
            state = state[0] as State,
            input = state[1] as SearchInput,
            searchResult = state[2] as SearchResult?,
            popularResults = state[3] as SearchResult?,
            collection = state[4] as UserCollectionState,
            navigateShow = state[5] as Show?,
            navigateMovie = state[6] as Movie?,
            navigatePerson = state[7] as Person?,
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
