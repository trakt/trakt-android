package tv.trakt.trakt.core.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.calendar.usecases.GetCalendarItemsUseCase
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import java.time.DayOfWeek.MONDAY
import java.time.Instant
import java.time.LocalDate

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class CalendarViewModel(
    private val sessionManager: SessionManager,
    private val getCalendarItemsUseCase: GetCalendarItemsUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
) : ViewModel() {
    private val initialState = CalendarState(
        selectedStartDay = LocalDate.now().with(MONDAY),
    )

    private val selectedStartDayState = MutableStateFlow(initialState.selectedStartDay)
    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadUser()
        loadData()
    }

    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }

        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                loadingState.update { DONE }
                return@launch
            }

            try {
                loadingState.update { LOADING }
                itemsState.update {
                    val currentDay = selectedStartDayState.value
                    getCalendarItemsUseCase.getCalendarItems(currentDay)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
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
    }

    val state = combine(
        selectedStartDayState,
        userState,
        itemsState,
        navigateShow,
        navigateMovie,
        navigateEpisode,
        loadingState,
        errorState,
    ) { states ->
        CalendarState(
            selectedStartDay = states[0] as LocalDate,
            user = states[1] as User?,
            items = states[2] as ImmutableMap<Instant, ImmutableList<HomeUpcomingItem>>?,
            navigateShow = states[3] as TraktId?,
            navigateMovie = states[4] as TraktId?,
            navigateEpisode = states[5] as Pair<TraktId, Episode>?,
            loading = states[6] as LoadingState,
            error = states[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
