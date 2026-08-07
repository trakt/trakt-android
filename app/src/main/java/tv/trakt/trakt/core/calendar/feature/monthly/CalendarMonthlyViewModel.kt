package tv.trakt.trakt.core.calendar.feature.monthly

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
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
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.feature.monthly.data.CalendarMonthKey
import tv.trakt.trakt.core.calendar.feature.monthly.data.CalendarMonthlyItemsCache
import tv.trakt.trakt.core.calendar.feature.monthly.usecases.GetMonthlyCalendarItemsUseCase
import tv.trakt.trakt.core.calendar.model.CalendarDayDisplay
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.model.CalendarItems
import tv.trakt.trakt.core.calendar.model.CalendarView
import tv.trakt.trakt.core.calendar.model.withWatched
import tv.trakt.trakt.core.calendar.model.withoutMovie
import tv.trakt.trakt.core.calendar.usecases.GetCalendarDisplayUseCase
import tv.trakt.trakt.core.calendar.usecases.GetCalendarTypeUseCase
import tv.trakt.trakt.core.calendar.usecases.GetCalendarViewUseCase
import tv.trakt.trakt.core.calendar.usecases.ObserveCalendarUpdatesUseCase
import tv.trakt.trakt.core.calendar.usecases.SaveCalendarMediaUseCase
import tv.trakt.trakt.core.calendar.usecases.UpdateCalendarHistoryUseCase
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.resources.R
import java.time.LocalDate
import java.time.YearMonth

@Suppress("UNCHECKED_CAST")
internal class CalendarMonthlyViewModel(
    private val sessionManager: SessionManager,
    private val filterManager: GlobalFilterManager,
    private val calendarCache: CalendarMonthlyItemsCache,
    private val getCalendarViewUseCase: GetCalendarViewUseCase,
    private val getCalendarDisplayUseCase: GetCalendarDisplayUseCase,
    private val getMonthlyCalendarItemsUseCase: GetMonthlyCalendarItemsUseCase,
    private val getCalendarTypeUseCase: GetCalendarTypeUseCase,
    private val saveCalendarMediaUseCase: SaveCalendarMediaUseCase,
    private val updateCalendarHistoryUseCase: UpdateCalendarHistoryUseCase,
    private val observeCalendarUpdatesUseCase: ObserveCalendarUpdatesUseCase,
) : ViewModel() {
    private val initialState = CalendarMonthlyState()

    private val selectedMonthState = MutableStateFlow(initialState.selectedMonth)
    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val typeState = MutableStateFlow(initialState.type)
    private val displayState = MutableStateFlow(initialState.display)
    private val itemsState = MutableStateFlow(initialState.items)
    private val itemsLoadingState = MutableStateFlow(initialState.itemsLoading)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)

    private var dataJob: Job? = null
    private var navigationJob: Job? = null
    private var processingJob: Job? = null

    // Seed the type from the persisted calendar selection once; changes here stay in-memory only.
    private var typeSeeded = false

    init {
        rememberView()
        loadDisplay()
        observeFilters()
        observeData()
    }

    // Persist the layout so the calendar reopens on the one last used.
    private fun rememberView() {
        viewModelScope.launch {
            getCalendarViewUseCase.setView(CalendarView.Monthly)
        }
    }

    private fun loadDisplay() {
        viewModelScope.launch {
            displayState.update { getCalendarDisplayUseCase.getDisplay() }
        }
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
            .onEach {
                calendarCache.clear()
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        val month = selectedMonthState.value ?: return

        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                loadingState.update { Done }
                return@launch
            }

            try {
                if (!typeSeeded) {
                    typeSeeded = true
                    typeState.update { getCalendarTypeUseCase.getType() }
                }

                val key = CalendarMonthKey(
                    month = month,
                    filter = filterState.value,
                    type = typeState.value,
                )

                // Already visited month - render from memory without a spinner or a refetch.
                calendarCache.get(key)?.let { cached ->
                    itemsState.update { cached }
                    return@launch
                }

                loadingState.update { Loading }

                itemsState.update {
                    val calendarItems = getMonthlyCalendarItemsUseCase.getCalendarItems(
                        month = month,
                        filters = filterState.value,
                        type = typeState.value,
                    )
                    calendarCache.put(key = key, items = calendarItems)
                    calendarItems
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

    fun setMonth(month: YearMonth) {
        if (selectedMonthState.value == month) {
            return
        }
        selectedMonthState.update { month }
        loadData()
    }

    fun setFilter(filter: GlobalFilter) {
        filterState.update { filter }
        loadData()
    }

    fun setDisplay(display: CalendarDayDisplay) {
        if (display == displayState.value) {
            return
        }
        displayState.update { display }
        viewModelScope.launch {
            getCalendarDisplayUseCase.setDisplay(display)
        }
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
            patchItems { it.withWatched(id = episode.ids.trakt, watched = true) }
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
            patchItems { it.withWatched(id = movie.ids.trakt, watched = true) }
        }
    }

    fun removeFromWatched(episode: Episode) {
        updateHistory(
            id = episode.ids.trakt,
            info = R.string.text_info_history_removed,
        ) {
            updateCalendarHistoryUseCase.removeFromWatched(episode)
            patchItems { it.withWatched(id = episode.ids.trakt, watched = false) }
        }
    }

    fun removeFromWatched(movie: Movie) {
        updateHistory(
            id = movie.ids.trakt,
            info = R.string.text_info_history_removed,
        ) {
            updateCalendarHistoryUseCase.removeFromWatched(movie)
            patchItems { it.withoutMovie(movie.ids.trakt) }
        }
    }

    private fun patchItems(transform: (CalendarItems) -> CalendarItems) {
        itemsState.update { it?.let(transform) }
        calendarCache.patch(transform)
    }

    private fun updateHistory(
        id: TraktId,
        @StringRes info: Int,
        action: suspend () -> Unit,
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
                action()
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

    fun clearInfo() {
        infoState.update { null }
    }

    // Navigation

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || navigationJob?.isActive == true) {
            return
        }
        navigationJob = viewModelScope.launch {
            saveCalendarMediaUseCase.saveShow(show)
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || navigationJob?.isActive == true) {
            return
        }
        navigationJob = viewModelScope.launch {
            saveCalendarMediaUseCase.saveMovie(movie)
            navigateMovie.update { movie.ids.trakt }
        }
    }

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (navigateEpisode.value != null || navigationJob?.isActive == true) {
            return
        }
        navigationJob = viewModelScope.launch {
            saveCalendarMediaUseCase.saveEpisode(show = show, episode = episode)
            navigateEpisode.update { Pair(show.ids.trakt, episode) }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
        navigateEpisode.update { null }
        navigationJob = null
    }

    override fun onCleared() {
        navigationJob?.cancel()
        navigationJob = null
        processingJob?.cancel()
        processingJob = null
    }

    val state = combine(
        selectedMonthState,
        filterState,
        typeState,
        displayState,
        itemsState,
        itemsLoadingState,
        navigateShow,
        navigateMovie,
        navigateEpisode,
        loadingState,
        infoState,
        errorState,
    ) { states ->
        CalendarMonthlyState(
            selectedMonth = states[0] as YearMonth?,
            filter = states[1] as GlobalFilter?,
            type = states[2] as ReleaseType,
            display = states[3] as CalendarDayDisplay,
            items = states[4] as ImmutableMap<LocalDate, ImmutableList<CalendarItem>>?,
            itemsLoading = states[5] as ImmutableSet<TraktId>?,
            navigateShow = states[6] as TraktId?,
            navigateMovie = states[7] as TraktId?,
            navigateEpisode = states[8] as Pair<TraktId, Episode>?,
            loading = states[9] as LoadingState,
            info = states[10] as DynamicStringResource?,
            error = states[11] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
