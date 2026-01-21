package tv.trakt.trakt.core.summary.movies

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
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
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.favorites.FavoritesUpdates
import tv.trakt.trakt.core.favorites.FavoritesUpdates.Source.DETAILS
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.AddPersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker
import tv.trakt.trakt.core.summary.movies.MovieDetailsState.UserRatingsState
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.movies.features.actors.usecases.GetMovieDirectorUseCase
import tv.trakt.trakt.core.summary.movies.navigation.MovieDetailsDestination
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieRatingsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieStudiosUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieFavoritesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

internal class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val appContext: Context,
    private val getDetailsUseCase: GetMovieDetailsUseCase,
    private val getExternalRatingsUseCase: GetMovieRatingsUseCase,
    private val getMovieStudiosUseCase: GetMovieStudiosUseCase,
    private val getMovieDirectorUseCase: GetMovieDirectorUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val loadListsUseCase: LoadUserListsUseCase,
    private val loadRatingUseCase: LoadUserRatingsUseCase,
    private val loadFavoritesUseCase: LoadUserFavoritesUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val updateMovieFavoritesUseCase: UpdateMovieFavoritesUseCase,
    private val addListItemUseCase: AddPersonalListItemUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userFavoritesLocalSource: UserFavoritesLocalDataSource,
    private val movieDetailsUpdates: MovieDetailsUpdates,
    private val favoritesUpdates: FavoritesUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<MovieDetailsDestination>()
    private val movieId = destination.movieId.toTraktId()

    private val initialState = MovieDetailsState()

    private val movieState = MutableStateFlow(initialState.movie)
    private val movieRatingsState = MutableStateFlow(initialState.movieRatings)
    private val movieUserRatingsState = MutableStateFlow(initialState.movieUserRating)
    private val movieStudiosState = MutableStateFlow(initialState.movieStudios)
    private val movieCreatorState = MutableStateFlow(initialState.movieCreator)
    private val movieProgressState = MutableStateFlow(initialState.movieProgress)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingProgress = MutableStateFlow(initialState.loadingProgress)
    private val loadingLists = MutableStateFlow(initialState.loadingLists)
    private val loadingFavorite = MutableStateFlow(initialState.loadingFavorite)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val userState = MutableStateFlow(initialState.user)
    private val metaCollapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.MOVIE_META))

    private var ratingJob: Job? = null
    private var metaCollapseJob: Job? = null

    init {
        loadUser()
        loadData()

        loadUserProgressData()
        loadUserRatingData()

        analytics.logScreenView(
            screenName = "movie_details",
        )
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
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

                loadRatings(movie)
                loadStudios()
                loadCreator()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private fun loadUserProgressData() {
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
                        val lists = loadListsUseCase.loadLocalLists()
                        lists.size to lists.values.flatten()
                            .firstOrNull {
                                it.type == MOVIE && it.id == movieId
                            }
                    }

                    val progress = progressAsync.await()
                    val watchlist = watchlistAsync.await()
                    val (listsCount, lists) = listsAsync.await()

                    movieProgressState.update {
                        MovieDetailsState.ProgressState(
                            plays = progress?.plays ?: 0,
                            inWatchlist = watchlist != null,
                            inLists = lists != null,
                            hasLists = listsCount > 0,
                        )
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingProgress.update { DONE }
                loadingLists.update { DONE }
            }
        }
    }

    private fun loadUserRatingData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                movieUserRatingsState.update {
                    UserRatingsState(
                        loading = LOADING,
                    )
                }

                coroutineScope {
                    val ratingsAsync = async {
                        if (!loadRatingUseCase.isMoviesLoaded()) {
                            loadRatingUseCase.loadMovies()
                        }
                    }

                    val favoritesAsync = async {
                        if (!loadFavoritesUseCase.isMoviesLoaded()) {
                            loadFavoritesUseCase.loadMovies()
                        }
                    }

                    ratingsAsync.await()
                    favoritesAsync.await()
                }

                val userRatings = loadRatingUseCase.loadLocalMovies()
                val userFavorites = loadFavoritesUseCase.loadLocalMovies()
                    .associateBy { it.movie.ids.trakt }

                val userRating = userRatings[movieId]?.copy(
                    favorite = userFavorites[movieId] != null,
                ) ?: UserRating(
                    mediaId = movieId,
                    mediaType = MOVIE,
                    rating = 0,
                    favorite = userFavorites[movieId] != null,
                )

                movieUserRatingsState.update {
                    UserRatingsState(
                        rating = userRating,
                        loading = DONE,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadRatings(movie: Movie?) {
        if (movie?.released?.isTodayOrBefore() != true) {
            // Don't load ratings for unreleased movies
            return
        }

        viewModelScope.launch {
            try {
                movieRatingsState.update {
                    getExternalRatingsUseCase.getExternalRatings(movieId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
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
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadCreator() {
        viewModelScope.launch {
            try {
                movieCreatorState.update {
                    getMovieDirectorUseCase.getDirector(movieId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun addToWatched(customDate: DateSelectionResult? = null) {
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

                val response = updateMovieHistoryUseCase.addToWatched(
                    movieId = movieId,
                    customDate = customDate,
                )
                loadProgressUseCase.loadMoviesProgress()
                userWatchlistLocalSource.removeMovies(
                    ids = setOf(movieId),
                    notify = true,
                )
                movieDetailsUpdates.notifyUpdate()

                if (response.added.movies != 0) {
                    movieProgressState.update {
                        it?.copy(
                            plays = it.plays + 1,
                            inWatchlist = false,
                        )
                    }
                    infoState.update {
                        DynamicStringResource(R.string.text_info_history_added)
                    }
                }

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "movie_details",
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    fun removeFromWatched() {
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

                updateMovieHistoryUseCase.removeAllFromHistory(movieId)
                loadProgressUseCase.loadMoviesProgress()

                movieProgressState.update {
                    it?.copy(
                        plays = 0,
                    )
                }
                movieDetailsUpdates.notifyUpdate()

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "movie",
                    source = "movie_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
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

                val plays = movieProgressState.value?.plays?.minus(1) ?: 0
                movieProgressState.update {
                    it?.copy(
                        plays = plays.coerceAtLeast(0),
                    )
                }
                movieDetailsUpdates.notifyUpdate()

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "movie",
                    source = "movie_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
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

    fun toggleFavorite(add: Boolean) {
        if (movieState.value == null ||
            loadingFavorite.value.isLoading
        ) {
            return
        }

        when {
            add -> addToFavorites()
            else -> removeFromFavorites()
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

                analytics.progress.logAddWatchlistMedia(
                    mediaType = "movie",
                    source = "movie_details",
                )
                infoState.update {
                    DynamicStringResource(R.string.text_info_watchlist_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
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
                analytics.progress.logRemoveWatchlistMedia(
                    mediaType = "movie",
                    source = "movie_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
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
                    Timber.recordError(error)
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
                    Timber.recordError(error)
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
                val lists = loadListsUseCase.loadLocalLists()
                lists.size to lists.values.flatten()
                    .firstOrNull {
                        it.type == MOVIE && it.id == movieId
                    }
            }

            val watchlist = watchlistAsync.await()
            val (listsCount, lists) = listsAsync.await()

            movieProgressState.update {
                it?.copy(
                    inWatchlist = watchlist != null,
                    inLists = lists != null,
                    hasLists = listsCount > 0,
                )
            }
        }
    }

    private fun addToFavorites() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                loadingFavorite.update { LOADING }

                delay(300) // Small delay to allow UI to settle.
                updateMovieFavoritesUseCase.addToFavorites(movieId)
                userFavoritesLocalSource.addMovies(
                    movies = listOf(
                        FavoriteItem.MovieItem(
                            rank = 0,
                            movie = movieState.value!!,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                )
                favoritesUpdates.notifyUpdate(DETAILS)

                movieUserRatingsState.update {
                    it?.copy(
                        rating = it.rating?.copy(
                            favorite = true,
                        ),
                    ) ?: UserRatingsState(
                        rating = UserRating(
                            mediaId = movieId,
                            mediaType = MOVIE,
                            rating = 0,
                            favorite = true,
                        ),
                    )
                }

                analytics.ratings.logFavoriteAdd(
                    mediaType = "movie",
                )
                infoState.update {
                    DynamicStringResource(R.string.text_info_favorites_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingFavorite.update { DONE }
            }
        }
    }

    private fun removeFromFavorites() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                loadingFavorite.update { LOADING }

                delay(300) // Small delay to allow UI to settle.
                updateMovieFavoritesUseCase.removeFromFavorites(movieId)
                userFavoritesLocalSource.removeMovies(setOf(movieId))
                favoritesUpdates.notifyUpdate(DETAILS)

                movieUserRatingsState.update {
                    it?.copy(
                        rating = it.rating?.copy(
                            favorite = false,
                        ),
                    ) ?: UserRatingsState(
                        rating = UserRating(
                            mediaId = movieId,
                            mediaType = MOVIE,
                            rating = 0,
                            favorite = false,
                        ),
                    )
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_favorites_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingFavorite.update { DONE }
            }
        }
    }

    fun addRating(newRating: Int) {
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            movieUserRatingsState.update {
                UserRatingsState(
                    rating = UserRating(
                        mediaId = movieId,
                        mediaType = MOVIE,
                        rating = newRating,
                        favorite = it?.rating?.favorite == true,
                    ),
                    loading = DONE,
                )
            }

            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = movieId,
                mediaType = MOVIE,
                rating = newRating,
            )
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    fun setMetaCollapsed(collapsed: Boolean) {
        metaCollapseState.update { collapsed }
        metaCollapseJob?.cancel()
        metaCollapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.MOVIE_META)
                else -> collapsingManager.expand(CollapsingKey.MOVIE_META)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        movieState,
        movieRatingsState,
        movieStudiosState,
        movieCreatorState,
        movieProgressState,
        movieUserRatingsState,
        loadingState,
        loadingProgress,
        loadingLists,
        loadingFavorite,
        infoState,
        errorState,
        userState,
        metaCollapseState,
    ) { state ->
        MovieDetailsState(
            movie = state[0] as Movie?,
            movieRatings = state[1] as ExternalRating?,
            movieStudios = state[2] as ImmutableList<String>?,
            movieCreator = state[3] as Person?,
            movieProgress = state[4] as MovieDetailsState.ProgressState?,
            movieUserRating = state[5] as UserRatingsState?,
            loading = state[6] as LoadingState,
            loadingProgress = state[7] as LoadingState,
            loadingLists = state[8] as LoadingState,
            loadingFavorite = state[9] as LoadingState,
            info = state[10] as StringResource?,
            error = state[11] as Exception?,
            user = state[12] as User?,
            metaCollapsed = state[13] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
