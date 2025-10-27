package tv.trakt.trakt.app.core.details.movie

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
import tv.trakt.trakt.app.core.inappreview.usecases.RequestAppReviewUseCase
import tv.trakt.trakt.app.core.tutorials.TutorialsManager
import tv.trakt.trakt.app.core.tutorials.model.TutorialKey.WATCH_NOW_MORE
import tv.trakt.trakt.common.auth.session.SessionManager
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
    private val getCollectionUseCase: GetCollectionUseCase,
    private val watchlistUseCase: ChangeWatchlistUseCase,
    private val historyUseCase: ChangeHistoryUseCase,
    private val appReviewUseCase: RequestAppReviewUseCase,
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
    }

    private fun loadData(movieId: TraktId) {
        viewModelScope.launch {
            try {
                val user = sessionManager.getProfile()
                val movie = getDetailsUseCase.getMovieDetails(movieId)
                movie?.let {
                    userState.update { user }
                    movieDetailsState.update { movie }

                    loadCollection(movieId)
                    loadStreamings(it.ids, user)

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
                    Timber.w("Error loading movie details: ${error.message}")
                }
            }
        }
    }

    private fun loadExternalRatings(movieId: TraktId) {
        viewModelScope.launch {
            try {
                val movieRatings = getExternalRatingsUseCase.getExternalRatings(movieId)
                movieRatingsState.value = movieRatings
            } catch (e: Exception) {
                e.rethrowCancellation {
                    showSnackMessage(StaticStringResource(e.toString()))
                    Timber.w("Error loading external ratings: ${e.message}")
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
                    Timber.w("Error loading extra videos: ${e.message}")
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
                    Timber.w("Error loading cast and crew: ${e.message}")
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
                    Timber.w("Error loading related movies: ${e.message}")
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
                    Timber.w("Error loading comments: ${e.message}")
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
                    Timber.w(e, "Error loading lists: ${e.message}")
                }
            }
        }
    }

    private fun loadStreamings(
        movieIds: Ids,
        user: User?,
    ) {
        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated() || user == null) {
                    return@launch
                }
                movieStreamingsState.update { it.copy(loading = true) }

                val plexService = getPlexUseCase.getPlexStatus(movieIds.trakt)
                if (plexService.isPlex) {
                    movieStreamingsState.update {
                        it.copy(
                            plex = true,
                            slug = plexService.plexSlug,
                            loading = false,
                            info = when {
                                !tutorialsManager.get(WATCH_NOW_MORE) ->
                                    DynamicStringResource(R.string.button_text_long_press_for_more)
                                else -> null
                            },
                        )
                    }
                    return@launch
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
                        info = when {
                            !tutorialsManager.get(WATCH_NOW_MORE) ->
                                DynamicStringResource(R.string.button_text_long_press_for_more)
                            else -> null
                        },
                    )
                }
            } catch (e: Exception) {
                e.rethrowCancellation {
                    showSnackMessage(StaticStringResource(e.toString()))
                    movieStreamingsState.update { StreamingsState() }
                    Timber.w("Error loading streaming services: ${e.message}")
                }
            }
        }
    }

    private fun loadCollection(movieId: TraktId) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                movieCollectionState.update {
                    it.copy(
                        isHistoryLoading = true,
                        isWatchlistLoading = true,
                    )
                }

                coroutineScope {
                    val watchedAsync = async { getCollectionUseCase.getWatchedMovie(movieId) }
                    val watchlistAsync = async { getCollectionUseCase.getWatchlistMovie(movieId) }

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
                    Timber.w("Error loading history: ${error.message}")
                }
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
                    Timber.w("Error toggling history: ${e.message}")
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
                    Timber.w("Error toggling watchlist: ${error.message}")
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
            tutorialsManager.acknowledge(WATCH_NOW_MORE)
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
