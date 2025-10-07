package tv.trakt.trakt.core.summary.movies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.sections.activity.all.data.local.AllActivityLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.usecases.AddPersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.summary.movies.navigation.MovieDetailsDestination
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieRatingsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieStudiosUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecase.lists.LoadUserListsUseCase
import tv.trakt.trakt.core.user.usecase.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase
import tv.trakt.trakt.resources.R

internal class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDetailsUseCase: GetMovieDetailsUseCase,
    private val getExternalRatingsUseCase: GetMovieRatingsUseCase,
    private val getMovieStudiosUseCase: GetMovieStudiosUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val loadListsUseCase: LoadUserListsUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val addListItemUseCase: AddPersonalListItemUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val allActivityLocalSource: AllActivityLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<MovieDetailsDestination>()
    private val movieId = destination.movieId.toTraktId()

    private val initialState = MovieDetailsState()

    private val movieState = MutableStateFlow(initialState.movie)
    private val movieRatingsState = MutableStateFlow(initialState.movieRatings)
    private val movieStudiosState = MutableStateFlow(initialState.movieStudios)
    private val movieProgressState = MutableStateFlow(initialState.movieProgress)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingProgress = MutableStateFlow(initialState.loadingProgress)
    private val loadingLists = MutableStateFlow(initialState.loadingLists)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val userState = MutableStateFlow(initialState.user)

    init {
        loadUser()
        loadData()
        loadProgressData()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                var movie = getDetailsUseCase.getLocalMovie(movieId)
                if (movie == null) {
                    loadingState.update { LOADING }
                    movie = getDetailsUseCase.getMovie(
                        movieId = movieId,
                    )
                }
                movieState.update { movie }

                loadRatings()
                loadStudios()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }
                loadingLists.update { LOADING }

                coroutineScope {
                    val progressAsync = async {
                        if (!loadProgressUseCase.isMoviesLoaded()) {
                            loadProgressUseCase.loadMoviesProgress()
                        }
                    }
                    val watchlistAsync = async {
                        if (!loadWatchlistUseCase.isMoviesLoaded()) {
                            loadWatchlistUseCase.loadWatchlist()
                        }
                    }
                    val listsAsync = async {
                        if (!loadListsUseCase.isLoaded()) {
                            loadListsUseCase.loadLists()
                        }
                    }
                    progressAsync.await()
                    watchlistAsync.await()
                    listsAsync.await()
                }

                coroutineScope {
                    val progressAsync = async {
                        loadProgressUseCase.loadLocalMovies()
                            .firstOrNull {
                                it.movie.ids.trakt == movieId
                            }
                    }

                    val watchlistAsync = async {
                        loadWatchlistUseCase.loadLocalMovies()
                            .firstOrNull {
                                it.movie.ids.trakt == movieId
                            }
                    }

                    val listsAsync = async {
                        loadListsUseCase.loadLocalLists()
                            .values
                            .flatten()
                            .firstOrNull {
                                it.type == MOVIE && it.id == movieId
                            }
                    }

                    val progress = progressAsync.await()
                    val watchlist = watchlistAsync.await()
                    val lists = listsAsync.await()

                    movieProgressState.update {
                        MovieDetailsState.ProgressState(
                            plays = progress?.plays ?: 0,
                            inWatchlist = watchlist != null,
                            inLists = lists != null,
                        )
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingProgress.update { DONE }
                loadingLists.update { DONE }
            }
        }
    }

    private fun loadRatings() {
        viewModelScope.launch {
            try {
                movieRatingsState.update {
                    getExternalRatingsUseCase.getExternalRatings(movieId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    private fun loadStudios() {
        viewModelScope.launch {
            try {
                movieStudiosState.update {
                    getMovieStudiosUseCase.getStudios(movieId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    fun addToWatched() {
        if (movieState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateMovieHistoryUseCase.addToWatched(movieId)
                loadProgressUseCase.loadMoviesProgress()
                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movieId),
                    notify = true,
                )
                allActivityLocalSource.notifyUpdate()

                movieProgressState.update {
                    it?.copy(
                        plays = it.plays + 1,
                        inWatchlist = false,
                    )
                }
                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    fun removeFromWatched(playId: Long) {
        if (movieState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateMovieHistoryUseCase.removePlayFromHistory(playId)
                loadProgressUseCase.loadMoviesProgress()
                allActivityLocalSource.notifyUpdate()

                val plays = movieProgressState.value?.plays?.minus(1) ?: 0
                movieProgressState.update {
                    it?.copy(
                        plays = plays.coerceAtLeast(0),
                    )
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    fun toggleWatchlist() {
        if (movieState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        if (movieProgressState.value?.inWatchlist == true) {
            removeFromWatchlist()
        } else {
            addToWatchlist()
        }
    }

    fun toggleList(
        listId: TraktId,
        add: Boolean,
    ) {
        if (movieState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        when {
            add -> addToList(listId)
            else -> removeFromList(listId)
        }
    }

    private fun addToWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                updateMovieWatchlistUseCase.addToWatchlist(movieId)
                userWatchlistLocalSource.addMovies(
                    movies = listOf(
                        WatchlistItem.MovieItem(
                            rank = 0,
                            movie = movieState.value!!,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                    notify = true,
                )

                movieProgressState.update {
                    it?.copy(
                        inWatchlist = true,
                    )
                }
                infoState.update {
                    DynamicStringResource(R.string.text_info_watchlist_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private fun removeFromWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                updateMovieWatchlistUseCase.removeFromWatchlist(movieId)
                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movieId),
                    notify = true,
                )

                refreshLists()
                infoState.update {
                    DynamicStringResource(R.string.text_info_watchlist_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private fun addToList(listId: TraktId) {
        viewModelScope.launch {
            val movie = movieState.value
            if (!sessionManager.isAuthenticated() || movie == null) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                addListItemUseCase.addMovie(
                    listId = listId,
                    movie = movie,
                )

                movieProgressState.update {
                    it?.copy(inLists = true)
                }
                infoState.update {
                    DynamicStringResource(R.string.text_info_list_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private fun removeFromList(listId: TraktId) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                removeListItemUseCase.removeMovie(
                    listId = listId,
                    movieId = movieId,
                )

                refreshLists()
                infoState.update {
                    DynamicStringResource(R.string.text_info_list_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private suspend fun refreshLists() {
        return coroutineScope {
            val watchlistAsync = async {
                loadWatchlistUseCase.loadLocalMovies()
                    .firstOrNull {
                        it.movie.ids.trakt == movieId
                    }
            }

            val listsAsync = async {
                loadListsUseCase.loadLocalLists()
                    .values
                    .flatten()
                    .firstOrNull {
                        it.type == MOVIE && it.id == movieId
                    }
            }

            val watchlist = watchlistAsync.await()
            val lists = listsAsync.await()

            movieProgressState.update {
                it?.copy(
                    inWatchlist = watchlist != null,
                    inLists = lists != null,
                )
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<MovieDetailsState> = combine(
        movieState,
        movieRatingsState,
        movieStudiosState,
        movieProgressState,
        loadingState,
        loadingProgress,
        loadingLists,
        infoState,
        errorState,
        userState,
    ) { state ->
        MovieDetailsState(
            movie = state[0] as Movie?,
            movieRatings = state[1] as ExternalRating?,
            movieStudios = state[2] as ImmutableList<String>?,
            movieProgress = state[3] as MovieDetailsState.ProgressState?,
            loading = state[4] as LoadingState,
            loadingProgress = state[5] as LoadingState,
            loadingLists = state[6] as LoadingState,
            info = state[7] as StringResource?,
            error = state[8] as Exception?,
            user = state[9] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
