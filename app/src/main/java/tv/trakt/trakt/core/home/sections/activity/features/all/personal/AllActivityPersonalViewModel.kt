@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.home.sections.activity.features.all.personal

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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_LIMIT
import tv.trakt.trakt.core.home.sections.activity.features.all.AllActivityState
import tv.trakt.trakt.core.home.sections.activity.features.all.navigation.AllPersonalActivityDestination
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.usecases.GetPersonalActivityUseCase
import tv.trakt.trakt.core.ratings.data.RatingsUpdates
import tv.trakt.trakt.core.ratings.data.RatingsUpdates.Source.POST_RATING
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
internal class AllActivityPersonalViewModel(
    savedStateHandle: SavedStateHandle,
    filterManager: GlobalFilterManager,
    analytics: Analytics,
    private val getActivityUseCase: GetPersonalActivityUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val userRatingsUseCase: LoadUserRatingsUseCase,
    private val showUpdatesSource: ShowDetailsUpdates,
    private val episodeUpdatesSource: EpisodeDetailsUpdates,
    private val movieDetailsUpdates: MovieDetailsUpdates,
    private val ratingsUpdates: RatingsUpdates,
    private val checkInUpdates: CheckInUpdates,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllPersonalActivityDestination>()
    private val initialState = AllActivityState()

    private val filterState = MutableStateFlow(
        when {
            destination.filtersEnabled -> filterManager.getFilter()
            else -> GlobalFilter.Default
        },
    )

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val itemsRatingsState = MutableStateFlow(initialState.itemsRatings)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)

    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(Idle)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages = 1
    private var hasMoreData = false

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadUser()
        loadInitialData()
        loadUserRatingData()

        observeData()
        observeRatings()

        analytics.logScreenView(
            screenName = "all_activity_personal",
        )
    }

    private fun observeData() {
        merge(
            showUpdatesSource.observeUpdates(Source.Progress),
            showUpdatesSource.observeUpdates(Source.Seasons),
            showUpdatesSource.observeUpdates(Source.WatchedUntil),
            episodeUpdatesSource.observeUpdates(EpisodeDetailsUpdates.Source.PROGRESS),
            episodeUpdatesSource.observeUpdates(EpisodeDetailsUpdates.Source.SEASON),
            episodeUpdatesSource.observeUpdates(EpisodeDetailsUpdates.Source.HISTORY),
            movieDetailsUpdates.observeUpdates(MovieDetailsUpdates.Source.Progress),
            movieDetailsUpdates.observeUpdates(MovieDetailsUpdates.Source.History),
            checkInUpdates.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach { loadData(ignoreErrors = true) }
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

    private fun loadInitialData() {
        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) return@launch

            try {
                val localItems = getActivityUseCase.getLocalPersonalActivity(
                    limit = HOME_ALL_LIMIT,
                    filter = filterState.value.mode,
                )

                itemsState
                    .update {
                        localItems
                            .groupBy { it.activityAt.toLocalDay() }
                            .mapValues { it.value.toImmutableList() }
                            .toImmutableMap()
                    }.also {
                        if (localItems.isEmpty()) {
                            loadingState.update { Loading }
                        }
                    }

                val remoteItems = getActivityUseCase.getPersonalActivity(
                    page = 1,
                    limit = HOME_ALL_LIMIT,
                    filter = filterState.value,
                    skipLocal = true,
                )

                itemsState
                    .update {
                        remoteItems
                            .groupBy { it.activityAt.toLocalDay() }
                            .mapValues { it.value.toImmutableList() }
                            .toImmutableMap()
                    }.also {
                        hasMoreData = remoteItems.size >= HOME_ALL_LIMIT
                    }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
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

                val remoteItems = getActivityUseCase.getPersonalActivity(
                    page = 1,
                    limit = HOME_ALL_LIMIT,
                    filter = filterState.value,
                    skipLocal = true,
                )

                itemsState
                    .update {
                        remoteItems
                            .groupBy { it.activityAt.toLocalDay() }
                            .mapValues { it.value.toImmutableList() }
                            .toImmutableMap()
                    }.also {
                        hasMoreData = remoteItems.size >= HOME_ALL_LIMIT
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

                val nextData = getActivityUseCase.getPersonalActivity(
                    page = pages + 1,
                    limit = HOME_ALL_LIMIT,
                    filter = filterState.value,
                    skipLocal = true,
                )

                itemsState.update { items ->
                    val currentItems = items ?: emptyMap()

                    val newItems = nextData
                        .groupBy { it.activityAt.toLocalDay() }
                        .mapValues { it.value.toImmutableList() }

                    (currentItems + newItems)
                        .toImmutableMap()
                }

                pages += 1
                hasMoreData = nextData.size >= HOME_ALL_LIMIT
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
                coroutineScope {
                    val moviesAsync = async {
                        if (!userRatingsUseCase.isMoviesLoaded()) {
                            userRatingsUseCase.loadMovies()
                        }
                    }

                    val episodesAsync = async {
                        if (!userRatingsUseCase.isEpisodesLoaded()) {
                            userRatingsUseCase.loadEpisodes()
                        }
                    }

                    episodesAsync.await()
                    moviesAsync.await()
                }

                val userMovieRatings = userRatingsUseCase.loadLocalMovies()
                val userEpisodesRatings = userRatingsUseCase.loadLocalEpisodes()

                itemsRatingsState.update {
                    (userMovieRatings + userEpisodesRatings)
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

    fun removeItem(item: HomeActivityItem) {
        itemsState.update {
            it?.mapValues { entry ->
                entry.value
                    .filterNot { existingItem -> existingItem.id == item.id }
                    .toImmutableList()
            }
                ?.filterNot { entry -> entry.value.isEmpty() }
                ?.toImmutableMap()
        }
    }

    fun setFilter(newFilter: GlobalFilter) {
        if (newFilter == filterState.value) {
            return
        }
        filterState.update { newFilter }
        loadData()
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
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
            showLocalDataSource.upsertShows(listOf(show))
            episodeLocalDataSource.upsertEpisodes(listOf(episode))

            navigateEpisode.update {
                Pair(show.ids.trakt, episode)
            }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                movieLocalDataSource.upsertMovies(listOf(movie))
                navigateMovie.update { movie.ids.trakt }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateEpisode.update { null }
        navigateMovie.update { null }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { persistentMapOf() }
            loadingState.update { Done }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { Idle }
        }

        return false
    }

    val state = combine(
        itemsState,
        itemsRatingsState,
        filterState,
        navigateShow,
        navigateEpisode,
        navigateMovie,
        loadingState,
        loadingMoreState,
        userState,
        errorState,
    ) { state ->
        AllActivityState(
            items = state[0] as ImmutableMap<LocalDate, ImmutableList<HomeActivityItem>>?,
            itemsRatings = state[1] as? ImmutableMap<String, UserRating>,
            itemsFilter = state[2] as GlobalFilter?,
            navigateShow = state[3] as TraktId?,
            navigateEpisode = state[4] as Pair<TraktId, Episode>?,
            navigateMovie = state[5] as TraktId?,
            loading = state[6] as LoadingState,
            loadingMore = state[7] as LoadingState,
            user = state[8] as? User,
            error = state[9] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
