package tv.trakt.trakt.app.core.details.movie

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.details.movie.MovieDetailsState.CollectionState
import tv.trakt.trakt.app.core.details.movie.MovieDetailsState.StreamingsState
import tv.trakt.trakt.app.core.details.movie.navigation.MovieDestination
import tv.trakt.trakt.app.core.details.movie.usecases.GetCastCrewUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetCommentsUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetCustomListsUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetExternalRatingsUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetExtraVideosUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetRelatedMoviesUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.collection.ChangeHistoryUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.collection.ChangeWatchlistUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.collection.GetCollectionUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.streamings.GetPlexUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.streamings.GetStreamingsUseCase
import tv.trakt.trakt.app.core.plex.usecase.DropPlaybackUseCase
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdates
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdates.Source.SCROBBLE_STOP_WORKER
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.tutorials.TutorialsManager
import tv.trakt.trakt.common.core.tutorials.model.TutorialKey
import tv.trakt.trakt.common.firebase.inappreview.RequestAppReviewUseCase
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.StaticStringResource
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.resources.R

internal class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDetailsUseCase: GetMovieDetailsUseCase,
    private val getExternalRatingsUseCase: GetExternalRatingsUseCase,
    private val getExtraVideosUseCase: GetExtraVideosUseCase,
    private val getCastCrewUseCase: GetCastCrewUseCase,
    private val getRelatedMoviesUseCase: GetRelatedMoviesUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val getListsUseCase: GetCustomListsUseCase,
    private val getStreamingsUseCase: GetStreamingsUseCase,
    private val getPlexUseCase: GetPlexUseCase,
    private val dropPlaybackUseCase: DropPlaybackUseCase,
    private val getCollectionUseCase: GetCollectionUseCase,
    private val watchlistUseCase: ChangeWatchlistUseCase,
    private val historyUseCase: ChangeHistoryUseCase,
    private val appReviewUseCase: RequestAppReviewUseCase,
    private val scrobbleUpdates: ScrobbleUpdates,
    private val sessionManager: SessionManager,
    private val tutorialsManager: TutorialsManager,
) : ViewModel() {
    private val initialState = MovieDetailsState()

    private val movieDetailsState = MutableStateFlow(initialState.movieDetails)
    private val movieRatingsState = MutableStateFlow(initialState.movieRatings)
    private val movieVideosState = MutableStateFlow(initialState.movieVideos)
    private val movieCastState = MutableStateFlow(initialState.movieCast)
    private val movieRelatedState = MutableStateFlow(initialState.movieRelated)
    private val movieCommentsState = MutableStateFlow(initialState.movieComments)
    private val movieListsState = MutableStateFlow(initialState.movieLists)
    private val movieStreamingsState = MutableStateFlow(initialState.movieStreamings)
    private val movieCollectionState = MutableStateFlow(initialState.movieCollection)
    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val reviewState = MutableStateFlow(initialState.isReviewRequest)
    private val snackMessageState = MutableStateFlow(initialState.snackMessage)

    private val movie = savedStateHandle.toRoute<MovieDestination>()

    init {
        loadData(TraktId(movie.movieId))
        observeData()
    }

    private fun loadData(movieId: TraktId) {
        viewModelScope.launch {
            try {
                val user = sessionManager.getProfile()
                val movie = getDetailsUseCase.getMovieDetails(movieId)
                movie?.let {
                    userState.update { user }
                    movieDetailsState.update { movie }

                    viewModelScope.launch {
                        val collectionAsync = async { loadCollection(movieId, force = false) }
                        val streamingsAsync = async { loadStreamings(it.ids, user) }
                        awaitAll(collectionAsync, streamingsAsync)
                    }

                    loadExternalRatings(movieId)
                    loadExtraVideos(movieId)
                    loadCastCrew(movieId)
                    loadComments(movieId)
                    loadRelatedMovies(movieId)
                    loadLists(movieId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading movie details: ${error.message}")
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeData() {
        merge(
            scrobbleUpdates.observeUpdates(SCROBBLE_STOP_WORKER),
        )
            .distinctUntilChanged()
            .debounce(250)
            .onEach {
                val movie = movieDetailsState.value ?: return@onEach
                val user = userState.value ?: return@onEach
                viewModelScope.launch {
                    val collectionAsync = async { loadCollection(movie.ids.trakt, force = true) }
                    val streamingsAsync = async { loadStreamings(movieIds = movie.ids, user = user) }
                    awaitAll(collectionAsync, streamingsAsync)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadExternalRatings(movieId: TraktId) {
        viewModelScope.launch {
            try {
                val movieRatings = getExternalRatingsUseCase.getExternalRatings(movieId)
                movieRatingsState.value = movieRatings
            } catch (e: Exception) {
                e.rethrowCancellation {
                    showSnackMessage(StaticStringResource(e.toString()))
                    Timber.e("Error loading external ratings: ${e.message}")
                }
            }
        }
    }

    private fun loadExtraVideos(movieId: TraktId) {
        viewModelScope.launch {
            try {
                val movieVideos = getExtraVideosUseCase.getExtraVideos(movieId)
                movieVideosState.value = movieVideos
            } catch (e: Exception) {
                e.rethrowCancellation {
                    showSnackMessage(StaticStringResource(e.toString()))
                    Timber.e("Error loading extra videos: ${e.message}")
                }
            }
        }
    }

    private fun loadCastCrew(movieId: TraktId) {
        viewModelScope.launch {
            try {
                val movieCast = getCastCrewUseCase.getCastCrew(movieId)
                movieCastState.value = movieCast
            } catch (e: Exception) {
                e.rethrowCancellation {
                    showSnackMessage(StaticStringResource(e.toString()))
                    Timber.e("Error loading cast and crew: ${e.message}")
                }
            }
        }
    }

    private fun loadRelatedMovies(movieId: TraktId) {
        viewModelScope.launch {
            try {
                val relatedMovies = getRelatedMoviesUseCase.getRelatedMovies(movieId)
                movieRelatedState.value = relatedMovies
            } catch (e: Exception) {
                e.rethrowCancellation {
                    showSnackMessage(StaticStringResource(e.toString()))
                    Timber.e("Error loading related movies: ${e.message}")
                }
            }
        }
    }

    private fun loadComments(movieId: TraktId) {
        viewModelScope.launch {
            try {
                val movieComments = getCommentsUseCase.getComments(movieId)
                movieCommentsState.value = movieComments
            } catch (e: Exception) {
                e.rethrowCancellation {
                    showSnackMessage(StaticStringResource(e.toString()))
                    Timber.e("Error loading comments: ${e.message}")
                }
            }
        }
    }

    private fun loadLists(movieId: TraktId) {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val officialListsAsync = async { getListsUseCase.getOfficialLists(movieId) }
                    val personalListsAsync = async { getListsUseCase.getPersonalLists(movieId) }

                    movieListsState.value = (
                        officialListsAsync.await() +
                            personalListsAsync.await()
                    ).toImmutableList()
                }
            } catch (e: Exception) {
                e.rethrowCancellation {
                    showSnackMessage(StaticStringResource(e.toString()))
                    Timber.e(e, "Error loading lists: ${e.message}")
                }
            }
        }
    }

    private suspend fun loadStreamings(
        movieIds: Ids,
        user: User?,
    ) {
        try {
            if (!sessionManager.isAuthenticated() || user == null) {
                return
            }
            movieStreamingsState.update { it.copy(loading = true) }

            val plexService = getPlexUseCase.getPlexStatus(movieIds.trakt)
            if (plexService.isPlex) {
                val plexStream = plexService.plexSlug
                    ?.let { getPlexUseCase.getPlexStreamUrl(movieIds.trakt) }

                movieStreamingsState.update {
                    it.copy(
                        plex = true,
                        plexStream = plexStream,
                        slug = plexService.plexSlug,
                        loading = false,
                        info = null,
                    )
                }
                return
            }

            val streamingService = getStreamingsUseCase.getStreamingService(
                user = user,
                movieId = movieIds.trakt,
            )

            movieStreamingsState.update {
                it.copy(
                    slug = movieIds.plex,
                    loading = false,
                    service = streamingService.streamingService,
                    noServices = streamingService.noServices,
                    info = null,
                )
            }
        } catch (e: Exception) {
            e.rethrowCancellation {
                showSnackMessage(StaticStringResource(e.toString()))
                movieStreamingsState.update { StreamingsState() }
                Timber.e("Error loading streaming services: ${e.message}")
            }
        }
    }

    private suspend fun loadCollection(
        movieId: TraktId,
        force: Boolean,
    ) {
        if (!sessionManager.isAuthenticated()) {
            return
        }

        try {
            movieCollectionState.update {
                it.copy(
                    isHistoryLoading = true,
                    isWatchlistLoading = true,
                )
            }

            coroutineScope {
                val watchedAsync = async { getCollectionUseCase.getWatchedMovie(movieId, force) }
                val watchlistAsync = async { getCollectionUseCase.getWatchlistMovie(movieId, force) }

                val watched = watchedAsync.await()
                val watchlist = watchlistAsync.await()

                movieCollectionState.update {
                    it.copy(
                        isHistoryLoading = false,
                        isWatchlistLoading = false,
                        isHistory = watched != null,
                        isWatchlist = watchlist != null,
                        historyCount = watched?.plays ?: 0,
                    )
                }
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                showSnackMessage(StaticStringResource(error.toString()))
                movieCollectionState.update { CollectionState() }
                Timber.e("Error loading history: ${error.message}")
            }
        }
    }

    fun toggleHistory() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated() || movieCollectionState.value.isHistoryLoading) {
                return@launch
            }
            try {
                movieCollectionState.update { it.copy(isHistoryLoading = true) }

                historyUseCase.addToHistory(
                    movieId = movie.movieId.toTraktId(),
                    plays = movieCollectionState.value.historyCount,
                )

                movieCollectionState.update {
                    it.copy(
                        isWatchlist = false,
                        isHistory = true,
                        isHistoryLoading = false,
                        historyCount = it.historyCount + 1,
                    )
                }

                requestReviewIfNeeded()
                showSnackMessage(DynamicStringResource(R.string.text_info_history_added))
            } catch (e: Exception) {
                e.rethrowCancellation {
                    showSnackMessage(StaticStringResource(e.toString()))
                    Timber.e("Error toggling history: ${e.message}")
                }
            }
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                movieCollectionState.update { it.copy(isWatchlistLoading = true) }

                if (movieCollectionState.value.isWatchlist) {
                    watchlistUseCase.removeFromWatchlist(movie.movieId.toTraktId())
                    movieCollectionState.update {
                        it.copy(isWatchlist = false, isWatchlistLoading = false)
                    }
                    showSnackMessage(DynamicStringResource(R.string.text_info_watchlist_removed))
                } else {
                    watchlistUseCase.addToWatchlist(movie.movieId.toTraktId())
                    movieCollectionState.update {
                        it.copy(isWatchlist = true, isWatchlistLoading = false)
                    }
                    showSnackMessage(DynamicStringResource(R.string.text_info_watchlist_added))
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error toggling watchlist: ${error.message}")
                }
            }
        }
    }

    fun dropMoviePlayback() {
        viewModelScope.launch {
            try {
                val movieIds = movieDetailsState.value?.ids ?: return@launch
                val user = userState.value ?: return@launch

                movieStreamingsState.update { it.copy(loading = true) }
                dropPlaybackUseCase.dropMoviePlayback(movieIds.trakt)

                loadStreamings(
                    movieIds = movieIds,
                    user = user,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    movieStreamingsState.update { it.copy(loading = false) }
                    Timber.e("Error dropping movie playback: ${error.message}")
                }
            }
        }
    }

    private suspend fun requestReviewIfNeeded() {
        appReviewUseCase.incrementCount()
        reviewState.update {
            appReviewUseCase.shouldRequest()
        }
    }

    private fun showSnackMessage(message: StringResource) {
        snackMessageState.update { message }
    }

    fun clearWatchNowTip() {
        viewModelScope.launch {
            tutorialsManager.acknowledge(TutorialKey.WATCH_NOW_MORE)
            movieStreamingsState.update { it.copy(info = null) }
        }
    }

    fun clearInfoMessage() {
        snackMessageState.update { null }
    }

    fun clearReviewRequest() {
        reviewState.update { false }
    }

    val state: StateFlow<MovieDetailsState> = combine(
        loadingState,
        movieDetailsState,
        movieRatingsState,
        movieVideosState,
        movieCastState,
        movieRelatedState,
        movieCommentsState,
        movieListsState,
        movieStreamingsState,
        movieCollectionState,
        userState,
        snackMessageState,
        reviewState,
    ) { states ->
        @Suppress("UNCHECKED_CAST")
        MovieDetailsState(
            isLoading = states[0] as Boolean,
            movieDetails = states[1] as Movie?,
            movieRatings = states[2] as ExternalRating?,
            movieVideos = states[3] as ImmutableList<ExtraVideo>?,
            movieCast = states[4] as ImmutableList<CastPerson>?,
            movieRelated = states[5] as ImmutableList<Movie>?,
            movieComments = states[6] as ImmutableList<Comment>?,
            movieLists = states[7] as ImmutableList<CustomList>?,
            movieStreamings = states[8] as StreamingsState,
            movieCollection = states[9] as CollectionState,
            user = states[10] as User?,
            snackMessage = states[11] as StringResource?,
            isReviewRequest = states[12] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
