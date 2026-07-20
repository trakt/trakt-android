package tv.trakt.trakt.core.summary.movies.features.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.MovieItem
import tv.trakt.trakt.core.ratings.data.RatingsUpdates
import tv.trakt.trakt.core.ratings.data.RatingsUpdates.Source.POST_RATING
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates.Source.History
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates.Source.Progress
import tv.trakt.trakt.core.summary.movies.features.history.navigation.MovieHistoryDestination
import tv.trakt.trakt.core.summary.movies.features.history.usecases.GetMovieHistoryUseCase
import tv.trakt.trakt.core.summary.movies.features.history.usecases.HISTORY_PAGE_LIMIT
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UNCHECKED_CAST")
@OptIn(FlowPreview::class)
internal class MovieHistoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHistoryUseCase: GetMovieHistoryUseCase,
    private val userRatingsUseCase: LoadUserRatingsUseCase,
    private val updateHistoryUseCase: UpdateMovieHistoryUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val movieDetailsUpdates: MovieDetailsUpdates,
    private val ratingsUpdates: RatingsUpdates,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<MovieHistoryDestination>()

    private val initialState = MovieHistoryState(
        media = MovieHistoryState.Media(
            id = destination.movieId.toTraktId(),
            title = destination.movieTitle,
            background = destination.backgroundUrl,
            watched = destination.watched,
        ),
    )

    private val itemsState = MutableStateFlow(initialState.items)
    private val itemsRatingsState = MutableStateFlow(initialState.itemsRatings)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(Idle)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages = 1
    private var hasMoreData = false

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadData()
        loadUserRatingData()

        observeLists()
        observeRatings()
    }

    private fun observeLists() {
        merge(
            movieDetailsUpdates.observeUpdates(Progress),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

    private fun observeRatings() {
        merge(
            ratingsUpdates.observeUpdates(POST_RATING),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadUserRatingData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        if (processingJob?.isActive == true) {
            return
        }

        pages = 1
        hasMoreData = false

        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val remoteItems = getHistoryUseCase.getHistory(
                    movieId = destination.movieId.toTraktId(),
                    pagination = Pagination(1, HISTORY_PAGE_LIMIT),
                )

                itemsState
                    .update {
                        remoteItems
                            .groupBy { it.activityAt.toLocalDay() }
                            .mapValues { it.value.toImmutableList() }
                            .toImmutableMap()
                    }.also {
                        hasMoreData = remoteItems.size >= HISTORY_PAGE_LIMIT
                    }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    fun loadMoreData() {
        if (itemsState.value.isNullOrEmpty() || !hasMoreData) {
            return
        }
        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                val nextData = getHistoryUseCase.getHistory(
                    movieId = destination.movieId.toTraktId(),
                    pagination = Pagination(pages + 1, HISTORY_PAGE_LIMIT),
                )

                itemsState.update { items ->
                    val currentItems = items ?: emptyMap()

                    val newItems = nextData
                        .groupBy { it.activityAt.toLocalDay() }
                        .mapValues { it.value.toImmutableList() }

                    val merged = currentItems.toMutableMap()
                    newItems.forEach { (date, list) ->
                        merged[date] = ((merged[date] ?: emptyList()) + list).toImmutableList()
                    }
                    merged.toImmutableMap()
                }

                pages += 1
                hasMoreData = nextData.size >= HISTORY_PAGE_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { Done }
            }
        }
    }

    private fun loadUserRatingData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                if (!userRatingsUseCase.isMoviesLoaded()) {
                    userRatingsUseCase.loadMovies()
                }

                itemsRatingsState.update {
                    userRatingsUseCase.loadLocalMovies()
                        .mapKeys { it.value.key }
                        .toImmutableMap()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun removeFromWatched(item: MovieItem) {
        if (processingJob?.isActive == true) {
            return
        }

        processingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingState.update { Loading }

                updateHistoryUseCase.removePlayFromHistory(item.id)
                loadProgressUseCase.loadMoviesProgress()
                movieDetailsUpdates.notifyUpdate(History)

                itemsState.update { items ->
                    (items ?: emptyMap())
                        .mapValues { entry ->
                            entry.value
                                .filterNot { it.id == item.id }
                                .toImmutableList()
                        }
                        .filterValues { it.isNotEmpty() }
                        .toImmutableMap()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                processingJob = null
            }
        }
    }

    val state = combine(
        itemsState,
        itemsRatingsState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        MovieHistoryState(
            media = initialState.media,
            items = state[0] as ImmutableMap<LocalDate, ImmutableList<MovieItem>>?,
            itemsRatings = state[1] as? ImmutableMap<String, UserRating>,
            loading = state[2] as LoadingState,
            loadingMore = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
