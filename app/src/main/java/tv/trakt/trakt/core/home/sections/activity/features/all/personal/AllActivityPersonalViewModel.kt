@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.home.sections.activity.features.all.personal

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
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_LIMIT
import tv.trakt.trakt.core.home.sections.activity.features.all.AllActivityState
import tv.trakt.trakt.core.home.sections.activity.features.all.navigation.AllPersonalActivityDestination
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.usecases.GetPersonalActivityUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.ratings.data.RatingsUpdates
import tv.trakt.trakt.core.ratings.data.RatingsUpdates.Source.POST_RATING
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase

@OptIn(FlowPreview::class)
internal class AllActivityPersonalViewModel(
    savedStateHandle: SavedStateHandle,
    modeManager: MediaModeManager,
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
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllPersonalActivityDestination>()
    private val initialState = AllActivityState()

    private val filterState = MutableStateFlow(
        when {
            destination.filtersEnabled -> modeManager.getMode()
            else -> MediaMode.MEDIA
        },
    )

    private val itemsState = MutableStateFlow(initialState.items)
    private val itemsRatingsState = MutableStateFlow(initialState.itemsRatings)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)

    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(IDLE)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages: Int = 1
    private var hasMoreData: Boolean = false
    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadData()
        loadUserRatingData()

        observeData()
        observeRatings()

        analytics.logScreenView(
            screenName = "all_activity_personal",
        )
    }

    private fun observeData() {
        merge(
            showUpdatesSource.observeUpdates(Source.PROGRESS),
            showUpdatesSource.observeUpdates(Source.SEASONS),
            episodeUpdatesSource.observeUpdates(EpisodeDetailsUpdates.Source.PROGRESS),
            episodeUpdatesSource.observeUpdates(EpisodeDetailsUpdates.Source.SEASON),
            movieDetailsUpdates.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    private fun observeRatings() {
        merge(
            ratingsUpdates.observeUpdates(POST_RATING),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadUserRatingData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        clear()
        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localItems = getActivityUseCase.getLocalPersonalActivity(
                    limit = HOME_ALL_LIMIT,
                    filter = filterState.value,
                )
                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                val remoteItems = getActivityUseCase.getPersonalActivity(
                    page = 1,
                    limit = HOME_ALL_LIMIT,
                    filter = filterState.value,
                )
                itemsState.update { remoteItems }

                hasMoreData = remoteItems.size >= HOME_ALL_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
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
                loadingMoreState.update { LOADING }

                val nextData = getActivityUseCase.getPersonalActivity(
                    page = pages + 1,
                    limit = HOME_ALL_LIMIT,
                    filter = filterState.value,
                )

                itemsState.update { items ->
                    items
                        ?.plus(nextData)
                        ?.distinctBy { it.id }
                        ?.toImmutableList()
                }

                pages += 1
                hasMoreData = nextData.size >= HOME_ALL_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { DONE }
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
            it?.filterNot { existingItem -> existingItem.id == item.id }
                ?.toImmutableList()
        }
    }

    fun setFilter(newFilter: MediaMode) {
        viewModelScope.launch {
            filterState.update { newFilter }
            loadData()
        }
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
            itemsState.update {
                emptyList<HomeActivityItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { IDLE }
        }

        return false
    }

    private fun clear() {
        pages = 1
        hasMoreData = true
        dataJob?.cancel()
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
        errorState,
    ) { state ->
        AllActivityState(
            items = state[0] as ImmutableList<HomeActivityItem>?,
            itemsRatings = state[1] as? ImmutableMap<String, UserRating>,
            itemsFilter = state[2] as MediaMode?,
            navigateShow = state[3] as TraktId?,
            navigateEpisode = state[4] as Pair<TraktId, Episode>?,
            navigateMovie = state[5] as TraktId?,
            loading = state[6] as LoadingState,
            loadingMore = state[7] as LoadingState,
            error = state[8] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
