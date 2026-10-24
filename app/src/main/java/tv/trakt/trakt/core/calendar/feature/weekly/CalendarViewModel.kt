package tv.trakt.trakt.core.calendar.feature.weekly

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableMap
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
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
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
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.feature.weekly.usecases.GetCalendarItemsUseCase
import tv.trakt.trakt.core.calendar.model.CalendarView
import tv.trakt.trakt.core.calendar.model.withEpisodeWatched
import tv.trakt.trakt.core.calendar.model.withMovieWatched
import tv.trakt.trakt.core.calendar.model.withoutMovie
import tv.trakt.trakt.core.calendar.navigation.CalendarDestination
import tv.trakt.trakt.core.calendar.usecases.GetCalendarTypeUseCase
import tv.trakt.trakt.core.calendar.usecases.GetCalendarViewUseCase
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
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val filterManager: GlobalFilterManager,
    private val getCalendarViewUseCase: GetCalendarViewUseCase,
    private val getCalendarItemsUseCase: GetCalendarItemsUseCase,
    private val getCalendarTypeUseCase: GetCalendarTypeUseCase,
    private val saveCalendarMediaUseCase: SaveCalendarMediaUseCase,
    private val updateCalendarHistoryUseCase: UpdateCalendarHistoryUseCase,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
) : ViewModel() {
    private val source = savedStateHandle.toRoute<CalendarDestination>().source
    private val origin = ChangeOrigin.unique()

    private val initialState = CalendarState(
        source = source,
        selectedStartDay = LocalDate.now().with(MONDAY),
    )

    private val selectedStartDayState = MutableStateFlow(initialState.selectedStartDay)
    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val typeState = MutableStateFlow(initialState.type)
    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val itemsLoadingState = MutableStateFlow(initialState.itemsLoading)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
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

        source.screenName?.let {
            analytics.logScreenView(screenName = it)
        }
    }

    // Persist the layout so the calendar reopens on the one last used.
    private fun rememberView() {
        viewModelScope.launch {
            getCalendarViewUseCase.setView(source = source, view = CalendarView.Weekly)
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
        dataChanges.observe(Kind.History, exclude = origin)
            .debounce(200.milliseconds)
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
            if (source.requiresAuth && !sessionManager.isAuthenticated()) {
                loadingState.update { Done }
                return@launch
            }

            try {
                loadingState.update { Loading }

                if (!typeSeeded) {
                    typeSeeded = true
                    typeState.update { getCalendarTypeUseCase.getType(source) }
                }

                itemsState.update {
                    val currentDay = selectedStartDayState.value
                    getCalendarItemsUseCase.getCalendarItems(
                        source = source,
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
                // A cancelled load can finish after its replacement started. Only the active
                // load may clear the job reference and the loading state.
                if (dataJob == coroutineContext.job) {
                    loadingState.update { Done }
                    dataJob = null
                }
            }
        }
    }

    fun refreshData() {
        loadData()
    }

    fun loadTodayData() {
        val today = LocalDate.now().with(MONDAY)
        if (selectedStartDayState.value != today) {
            selectedStartDayState.update { today }
            loadData()
        }
    }

    fun logPeriodChange(action: String) {
        analytics.logCalendarPeriod(action)
    }

    fun loadWeek(day: LocalDate) {
        selectedStartDayState.update { day.with(MONDAY) }
        loadData()
    }

    // Appends the week following the last loaded one; progress is already in
    // memory from the initial load, so the use case skips reloading it.
    fun loadMoreData() {
        if (loadingState.value.isLoading || loadingMoreState.value.isLoading) {
            return
        }
        val currentItems = itemsState.value
        if (currentItems.isNullOrEmpty()) {
            return
        }

        viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                val nextWeekStart = currentItems.keys.max().plusDays(1)
                val nextItems = getCalendarItemsUseCase.getCalendarItems(
                    source = source,
                    day = nextWeekStart,
                    filters = filterState.value,
                    type = typeState.value,
                    skipProgress = true,
                )

                itemsState.update { items ->
                    (items.orEmpty() + nextItems).toImmutableMap()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    infoState.update {
                        DynamicStringResource(R.string.error_text_unexpected_error_short)
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { Done }
            }
        }
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
            updateCalendarHistoryUseCase.addToHistory(
                episode = episode,
                source = source,
                origin = origin,
                customDate = customDate,
            )
            itemsState.update { it?.withEpisodeWatched(id = episode.ids.trakt, watched = true) }
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
            updateCalendarHistoryUseCase.addToHistory(
                movie = movie,
                source = source,
                origin = origin,
                customDate = customDate,
            )
            itemsState.update { it?.withMovieWatched(id = movie.ids.trakt, watched = true) }
        }
    }

    fun removeFromWatched(episode: Episode) {
        updateHistory(
            id = episode.ids.trakt,
            info = R.string.text_info_history_removed,
        ) {
            updateCalendarHistoryUseCase.removeFromWatched(
                episode = episode,
                source = source,
                origin = origin,
            )
            itemsState.update { it?.withEpisodeWatched(id = episode.ids.trakt, watched = false) }
        }
    }

    fun removeFromWatched(movie: Movie) {
        updateHistory(
            id = movie.ids.trakt,
            info = R.string.text_info_history_removed,
        ) {
            updateCalendarHistoryUseCase.removeFromWatched(
                movie = movie,
                source = source,
                origin = origin,
            )
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
        loadingMoreState,
        infoState,
        errorState,
        typeState,
    ) { states ->
        CalendarState(
            source = source,
            selectedStartDay = states[0] as LocalDate,
            filter = states[1] as GlobalFilter?,
            user = states[2] as User?,
            items = states[3] as ImmutableMap<LocalDate, ImmutableList<CalendarItem>>?,
            itemsLoading = states[4] as ImmutableSet<TraktId>?,
            navigateShow = states[5] as TraktId?,
            navigateMovie = states[6] as TraktId?,
            navigateEpisode = states[7] as Pair<TraktId, Episode>?,
            loading = states[8] as LoadingState,
            loadingMore = states[9] as LoadingState,
            info = states[10] as DynamicStringResource?,
            error = states[11] as Exception?,
            type = states[12] as ReleaseType,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
