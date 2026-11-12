package tv.trakt.trakt.core.summary.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.EpisodeLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.activity.HomeActivityItem
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.history.model.MediaHistoryType
import tv.trakt.trakt.core.summary.history.navigation.MediaHistoryDestination
import tv.trakt.trakt.core.summary.history.usecases.GetMediaHistoryUseCase
import tv.trakt.trakt.core.summary.history.usecases.HISTORY_PAGE_LIMIT
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.resources.R
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UNCHECKED_CAST")
@OptIn(FlowPreview::class)
internal class MediaHistoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHistoryUseCase: GetMediaHistoryUseCase,
    private val userRatingsUseCase: LoadUserRatingsUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val showLocalSource: ShowLocalSource,
    private val episodeLocalSource: EpisodeLocalSource,
    private val dataChanges: DataChanges,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val origin = ChangeOrigin.unique()

    private val destination = savedStateHandle.toRoute<MediaHistoryDestination>()
    private val mediaType = destination.mediaType
    private val mediaId = destination.mediaId.toTraktId()

    private val initialState = MediaHistoryState(
        media = MediaHistoryState.Media(
            id = mediaId,
            type = mediaType,
            title = destination.mediaTitle,
            background = destination.backgroundUrl,
            watched = destination.watched,
        ),
    )

    private val itemsState = MutableStateFlow(initialState.items)
    private val itemsRatingsState = MutableStateFlow(initialState.itemsRatings)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(Idle)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val errorState = MutableStateFlow(initialState.error)

    private val infoFlow = MutableSharedFlow<StringResource>(replay = 0)
    val info = infoFlow.asSharedFlow()

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
        dataChanges.observe(Kind.History, exclude = origin)
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

    private fun observeRatings() {
        dataChanges.observe(Kind.Ratings)
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
                    mediaType = mediaType,
                    mediaId = mediaId,
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
                    mediaType = mediaType,
                    mediaId = mediaId,
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
                itemsRatingsState.update {
                    loadUserRatings()
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

    private suspend fun loadUserRatings(): Map<TraktId, UserRating> {
        return when (mediaType) {
            MediaHistoryType.Movie -> {
                if (!userRatingsUseCase.isMoviesLoaded()) {
                    userRatingsUseCase.loadMovies()
                }
                userRatingsUseCase.loadLocalMovies()
            }
            MediaHistoryType.Show, MediaHistoryType.Episode -> {
                if (!userRatingsUseCase.isEpisodesLoaded()) {
                    userRatingsUseCase.loadEpisodes()
                }
                userRatingsUseCase.loadLocalEpisodes()
            }
        }
    }

    fun removeFromWatched(item: HomeActivityItem) {
        if (processingJob?.isActive == true) {
            return
        }

        processingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingState.update { Loading }

                when (item) {
                    is HomeActivityItem.MovieItem -> updateMovieHistoryUseCase.removePlayFromHistory(
                        playId = item.id,
                        movieId = item.movie.ids.trakt,
                        origin = origin,
                    )
                    is HomeActivityItem.EpisodeItem -> updateEpisodeHistoryUseCase.removePlayFromHistory(
                        playId = item.id,
                        episodeId = item.episode.ids.trakt,
                        origin = origin,
                    )
                }

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

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (navigateEpisode.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalSource.upsertShows(listOf(show))
            episodeLocalSource.upsertEpisodes(listOf(episode))

            navigateEpisode.update {
                Pair(show.ids.trakt, episode)
            }
        }
    }

    fun clearNavigation() {
        navigateEpisode.update { null }
    }

    fun removeAllFromWatched() {
        if (processingJob?.isActive == true) {
            return
        }

        dataJob?.cancel()
        processingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingState.update { Loading }

                when (mediaType) {
                    MediaHistoryType.Movie -> updateMovieHistoryUseCase.removeAllFromHistory(
                        movieId = mediaId,
                        origin = origin,
                    )
                    MediaHistoryType.Show -> updateShowHistoryUseCase.removeAllFromHistory(
                        showId = mediaId,
                        origin = origin,
                    )
                    MediaHistoryType.Episode -> updateEpisodeHistoryUseCase.removeEpisodeFromHistory(
                        episodeId = mediaId.value,
                        origin = origin,
                    )
                }

                itemsState.update { persistentMapOf() }
                hasMoreData = false

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = mediaType.value,
                    source = "${mediaType.value}_history",
                )

                infoFlow.emit(DynamicStringResource(R.string.text_info_history_removed))
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
        navigateEpisode,
        errorState,
    ) { state ->
        MediaHistoryState(
            media = initialState.media,
            items = state[0] as ImmutableMap<LocalDate, ImmutableList<HomeActivityItem>>?,
            itemsRatings = state[1] as? ImmutableMap<String, UserRating>,
            loading = state[2] as LoadingState,
            loadingMore = state[3] as LoadingState,
            navigateEpisode = state[4] as Pair<TraktId, Episode>?,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
