package tv.trakt.trakt.core.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
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
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.usecases.GetCalendarItemsUseCase
import tv.trakt.trakt.core.calendar.usecases.GetCalendarTypeUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.ReleaseType
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.CALENDAR
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.History
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.Progress
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.Season
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.resources.R
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class CalendarViewModel(
    private val sessionManager: SessionManager,
    private val ratePromptManager: RatePromptManager,
    private val filterManager: GlobalFilterManager,
    private val getCalendarItemsUseCase: GetCalendarItemsUseCase,
    private val getCalendarTypeUseCase: GetCalendarTypeUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val showUpdates: ShowDetailsUpdates,
    private val episodeUpdates: EpisodeDetailsUpdates,
    private val movieUpdates: MovieDetailsUpdates,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = CalendarState(
        selectedStartDay = LocalDate.now().with(MONDAY),
    )

    private val selectedStartDayState = MutableStateFlow(initialState.selectedStartDay)
    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val typeState = MutableStateFlow(initialState.type)
    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val itemsLoadingState = MutableStateFlow(initialState.itemsLoading)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    // Seed the type from the persisted calendar selection once; changes here stay in-memory only.
    private var typeSeeded = false

    init {
        loadUser()
        loadData()

        observeUser()
        observeFilters()
        observeData()
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    private fun observeFilters() {
        filterManager.observeFilter()
            .onEach { value ->
                filterState.update { value }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        merge(
            showUpdates.observeUpdates(Source.Progress),
            showUpdates.observeUpdates(Source.Seasons),
            showUpdates.observeUpdates(Source.WatchedUntil),
            episodeUpdates.observeUpdates(Progress),
            episodeUpdates.observeUpdates(Season),
            episodeUpdates.observeUpdates(History),
            movieUpdates.observeUpdates(MovieDetailsUpdates.Source.Progress),
            movieUpdates.observeUpdates(MovieDetailsUpdates.Source.History),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadData()
            }.launchIn(viewModelScope)
    }

    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                loadingState.update { Done }
                return@launch
            }

            try {
                loadingState.update { Loading }

                if (!typeSeeded) {
                    typeSeeded = true
                    typeState.update { getCalendarTypeUseCase.getType() }
                }

                itemsState.update {
                    val currentDay = selectedStartDayState.value
                    getCalendarItemsUseCase.getCalendarItems(
                        day = currentDay,
                        filters = filterState.value,
                        type = typeState.value,
                    )
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

    fun loadTodayData() {
        val today = LocalDate.now().with(MONDAY)
        if (selectedStartDayState.value != today) {
            selectedStartDayState.update { today }
            loadData()
        }
    }

    fun loadNextWeekData() {
        val newStartDay = selectedStartDayState.value.plusWeeks(1)
        selectedStartDayState.update { newStartDay }
        loadData()
    }

    fun loadPreviousWeekData() {
        val newStartDay = selectedStartDayState.value.minusWeeks(1)
        selectedStartDayState.update { newStartDay }
        loadData()
    }

    fun setFilter(filter: GlobalFilter) {
        filterState.update { filter }
        loadData()
    }

    fun setType(type: ReleaseType) {
        if (type == typeState.value || loadingState.value.isLoading) {
            return
        }
        typeState.update { type }
        loadData()
    }

    // Mutations

    fun addToHistory(
        episode: Episode,
        customDate: DateSelectionResult? = null,
    ) {
        if (processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val authenticated = sessionManager.isAuthenticated()
                if (!authenticated) {
                    return@launch
                }

                itemsLoadingState.update {
                    persistentSetOf(episode.ids.trakt)
                }

                updateEpisodeHistoryUseCase.addToHistory(
                    episodeId = episode.ids.trakt,
                    customDate = customDate,
                )
                loadUserProgressUseCase.loadShowsProgress()
                episodeUpdates.notifyUpdate(CALENDAR)

                itemsState.update {
                    val currentItems = it ?: return@update it
                    currentItems.mapValues { entry ->
                        entry.value.map { item ->
                            if (item is CalendarItem.EpisodeItem && item.id == episode.ids.trakt) {
                                item.copy(watched = true)
                            } else {
                                item
                            }
                        }.toImmutableList()
                    }.toImmutableMap()
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }

                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "calendar",
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                itemsLoadingState.update { EmptyImmutableSet }
                processingJob = null
            }
        }
    }

    fun addToHistory(
        movie: Movie,
        customDate: DateSelectionResult? = null,
    ) {
        if (processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val authenticated = sessionManager.isAuthenticated()
                if (!authenticated) {
                    return@launch
                }

                itemsLoadingState.update {
                    persistentSetOf(movie.ids.trakt)
                }

                updateMovieHistoryUseCase.addToWatched(
                    movieId = movie.ids.trakt,
                    customDate = customDate,
                )
                loadUserProgressUseCase.loadMoviesProgress()
                episodeUpdates.notifyUpdate(CALENDAR)

                itemsState.update {
                    val currentItems = it ?: return@update it
                    currentItems.mapValues { entry ->
                        entry.value.map { item ->
                            if (item is CalendarItem.MovieItem && item.id == movie.ids.trakt) {
                                item.copy(watched = true)
                            } else {
                                item
                            }
                        }.toImmutableList()
                    }.toImmutableMap()
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }

                analytics.progress.logAddWatchedMedia(
                    mediaType = "movie",
                    source = "calendar",
                    date = customDate?.analyticsStrings,
                )

                ratePromptManager.checkMovies()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                itemsLoadingState.update { EmptyImmutableSet }
                processingJob = null
            }
        }
    }

    fun removeFromWatched(episode: Episode) {
        if (processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            val authenticated = sessionManager.isAuthenticated()
            if (!authenticated) {
                return@launch
            }

            itemsLoadingState.update {
                persistentSetOf(episode.ids.trakt)
            }

            try {
                updateEpisodeHistoryUseCase.removeEpisodeFromHistory(episode.ids.trakt.value)
                loadUserProgressUseCase.loadShowsProgress()
                episodeUpdates.notifyUpdate(CALENDAR)

                itemsState.update {
                    val currentItems = it ?: return@update it
                    currentItems.mapValues { entry ->
                        entry.value.map { item ->
                            if (item is CalendarItem.EpisodeItem && item.id == episode.ids.trakt) {
                                item.copy(watched = false)
                            } else {
                                item
                            }
                        }.toImmutableList()
                    }.toImmutableMap()
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "episode",
                    source = "calendar",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                itemsLoadingState.update { EmptyImmutableSet }
                processingJob = null
            }
        }
    }

    fun removeFromWatched(movie: Movie) {
        if (processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            val authenticated = sessionManager.isAuthenticated()
            if (!authenticated) {
                return@launch
            }

            itemsLoadingState.update {
                persistentSetOf(movie.ids.trakt)
            }

            try {
                updateMovieHistoryUseCase.removeAllFromHistory(movie.ids.trakt)
                loadUserProgressUseCase.loadMoviesProgress()
                episodeUpdates.notifyUpdate(CALENDAR)

                itemsState.update {
                    val currentItems = it ?: return@update it
                    currentItems.mapValues { entry ->
                        entry.value.filter { item ->
                            if (item is CalendarItem.MovieItem) {
                                item.id != movie.ids.trakt
                            } else {
                                true
                            }
                        }.toImmutableList()
                    }.toImmutableMap()
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }

                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "movie",
                    source = "calendar",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                itemsLoadingState.update { EmptyImmutableSet }
                processingJob = null
            }
        }
    }

    // Navigation

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
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

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
        navigateEpisode.update { null }
        processingJob = null
    }

    fun clearInfo() {
        infoState.update { null }
    }

    override fun onCleared() {
        processingJob?.cancel()
        processingJob = null
    }

    val state = combine(
        selectedStartDayState,
        filterState,
        userState,
        itemsState,
        itemsLoadingState,
        navigateShow,
        navigateMovie,
        navigateEpisode,
        loadingState,
        infoState,
        errorState,
        typeState,
    ) { states ->
        CalendarState(
            selectedStartDay = states[0] as LocalDate,
            filter = states[1] as GlobalFilter?,
            user = states[2] as User?,
            items = states[3] as ImmutableMap<LocalDate, ImmutableList<CalendarItem>>?,
            itemsLoading = states[4] as ImmutableSet<TraktId>?,
            navigateShow = states[5] as TraktId?,
            navigateMovie = states[6] as TraktId?,
            navigateEpisode = states[7] as Pair<TraktId, Episode>?,
            loading = states[8] as LoadingState,
            info = states[9] as DynamicStringResource?,
            error = states[10] as Exception?,
            type = states[11] as ReleaseType,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
