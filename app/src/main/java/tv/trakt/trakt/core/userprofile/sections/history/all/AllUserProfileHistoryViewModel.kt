@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.userprofile.sections.history.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_LIMIT
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.userprofile.UserProfileConfig.ALL_PAGE_LIMIT
import tv.trakt.trakt.core.userprofile.sections.history.usecases.GetUserProfileHistoryUseCase
import java.time.LocalDate

internal class AllUserProfileHistoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHistoryUseCase: GetUserProfileHistoryUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllUserProfileHistoryDestination>()
    private val userId = destination.userId.toTraktId()

    private val initialState = AllUserProfileHistoryState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filtersState = MutableStateFlow(initialState.filters)
    private val navigateShowState = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisodeState = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovieState = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages = 1
    private var hasMoreData = false

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val remoteItems = getHistoryUseCase.getUserHistory(
                    userId = userId,
                    pagination = Pagination(page = 1, limit = ALL_PAGE_LIMIT),
                    filters = filtersState.value,
                )

                itemsState
                    .update {
                        remoteItems
                            .groupBy { it.activityAt.toLocalDay() }
                            .mapValues { it.value.toImmutableList() }
                            .toImmutableMap()
                    }.also {
                        hasMoreData = remoteItems.size >= ALL_PAGE_LIMIT
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

                val remoteItems = getHistoryUseCase.getUserHistory(
                    userId = userId,
                    pagination = Pagination(
                        page = 1,
                        limit = ALL_PAGE_LIMIT,
                    ),
                    filters = filtersState.value,
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

                val nextData = getHistoryUseCase.getUserHistory(
                    userId = userId,
                    pagination = Pagination(
                        page = pages + 1,
                        limit = ALL_PAGE_LIMIT,
                    ),
                    filters = filtersState.value,
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

    fun navigateToShow(show: Show) {
        if (navigateShowState.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShowState.update { show.ids.trakt }
        }
    }

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (navigateEpisodeState.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            episodeLocalDataSource.upsertEpisodes(listOf(episode))
            navigateEpisodeState.update { Pair(show.ids.trakt, episode) }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovieState.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                movieLocalDataSource.upsertMovies(listOf(movie))
                navigateMovieState.update { movie.ids.trakt }
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
        navigateShowState.update { null }
        navigateEpisodeState.update { null }
        navigateMovieState.update { null }
    }

    fun setFilter(mode: MediaMode) {
        if (mode == filtersState.value.mode) {
            return
        }
        filtersState.update { it.copy(mode = mode) }
        loadData()
    }

    val state = combine(
        itemsState,
        navigateShowState,
        navigateEpisodeState,
        navigateMovieState,
        loadingState,
        loadingMoreState,
        filtersState,
        errorState,
    ) { state ->
        AllUserProfileHistoryState(
            items = state[0] as ImmutableMap<LocalDate, ImmutableList<HomeActivityItem>>?,
            navigateShow = state[1] as TraktId?,
            navigateEpisode = state[2] as Pair<TraktId, Episode>?,
            navigateMovie = state[3] as TraktId?,
            loading = state[4] as LoadingState,
            loadingMore = state[5] as LoadingState,
            filters = state[6] as GlobalFilter,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
