package tv.trakt.trakt.core.calendar.feature.weekly

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
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
import tv.trakt.trakt.common.auth.session.SessionManager
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
import tv.trakt.trakt.core.calendar.feature.weekly.usecases.GetCalendarItemsUseCase
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.model.CalendarView
import tv.trakt.trakt.core.calendar.model.withWatched
import tv.trakt.trakt.core.calendar.model.withoutMovie
import tv.trakt.trakt.core.calendar.usecases.GetCalendarTypeUseCase
import tv.trakt.trakt.core.calendar.usecases.GetCalendarViewUseCase
import tv.trakt.trakt.core.calendar.usecases.ObserveCalendarUpdatesUseCase
import tv.trakt.trakt.core.calendar.usecases.SaveCalendarMediaUseCase
import tv.trakt.trakt.core.calendar.usecases.UpdateCalendarHistoryUseCase
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.resources.R
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class CalendarViewModel(
    private val sessionManager: SessionManager,
    private val filterManager: GlobalFilterManager,
    private val getCalendarViewUseCase: GetCalendarViewUseCase,
    private val getCalendarItemsUseCase: GetCalendarItemsUseCase,
    private val getCalendarTypeUseCase: GetCalendarTypeUseCase,
    private val saveCalendarMediaUseCase: SaveCalendarMediaUseCase,
    private val observeCalendarUpdatesUseCase: ObserveCalendarUpdatesUseCase,
    private val updateCalendarHistoryUseCase: UpdateCalendarHistoryUseCase,
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
        rememberView()
        loadUser()
        loadData()

        observeUser()
        observeFilters()
        observeData()
    }

    // Persist the layout so the calendar reopens on the one last used.
    private fun rememberView() {
        viewModelScope.launch {
            getCalendarViewUseCase.setView(CalendarView.Weekly)
        }
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach { user -> userState.update { user } }
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
        observeCalendarUpdatesUseCase.observeUpdates()
            .onEach { loadData() }
            .launchIn(viewModelScope)
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
        updateHistory(
            id = episode.ids.trakt,
            info = R.string.text_info_history_added,
        ) {
            updateCalendarHistoryUseCase.addToHistory(episode = episode, customDate = customDate)
            itemsState.update { it?.withWatched(id = episode.ids.trakt, watched = true) }
        }
    }

    fun addToHistory(
        movie: Movie,
        customDate: DateSelectionResult? = null,
    ) {
        updateHistory(
            id = movie.ids.trakt,
            info = R.string.text_info_history_added,
        ) {
            updateCalendarHistoryUseCase.addToHistory(movie = movie, customDate = customDate)
            itemsState.update { it?.withWatched(id = movie.ids.trakt, watched = true) }
        }
    }

    fun removeFromWatched(episode: Episode) {
        updateHistory(
            id = episode.ids.trakt,
            info = R.string.text_info_history_removed,
        ) {
            updateCalendarHistoryUseCase.removeFromWatched(episode)
            itemsState.update { it?.withWatched(id = episode.ids.trakt, watched = false) }
        }
    }

    fun removeFromWatched(movie: Movie) {
        updateHistory(
            id = movie.ids.trakt,
            info = R.string.text_info_history_removed,
        ) {
            updateCalendarHistoryUseCase.removeFromWatched(movie)
            itemsState.update { it?.withoutMovie(movie.ids.trakt) }
        }
    }

    private fun updateHistory(
        id: TraktId,
        @StringRes info: Int,
        block: suspend () -> Unit,
    ) {
        if (processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                itemsLoadingState.update { persistentSetOf(id) }
                block()
                infoState.update { DynamicStringResource(info) }
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
            saveCalendarMediaUseCase.saveShow(show)
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            saveCalendarMediaUseCase.saveMovie(movie)
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
            saveCalendarMediaUseCase.saveEpisode(show = show, episode = episode)

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
