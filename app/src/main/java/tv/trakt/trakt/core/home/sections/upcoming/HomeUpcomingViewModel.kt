package tv.trakt.trakt.core.home.sections.upcoming

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.auth.session.helpers.distinctUntilSessionChanged
import tv.trakt.trakt.common.auth.session.helpers.observeSessionChanges
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.EpisodeLocalSource
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.observeForegroundRefresh
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarSource
import tv.trakt.trakt.core.calendar.usecases.GetCalendarTypeUseCase
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.sections.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.helpers.collapsing.CollapseDelegate
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import tv.trakt.trakt.helpers.navigation.MediaNavigationDelegate
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UNCHECKED_CAST")
@OptIn(FlowPreview::class)
internal class HomeUpcomingViewModel(
    private val appContext: Context,
    private val filterManager: GlobalFilterManager,
    private val getUpcomingUseCase: GetUpcomingUseCase,
    private val getCalendarTypeUseCase: GetCalendarTypeUseCase,
    private val dataChanges: DataChanges,
    private val sessionManager: SessionManager,
    private val collapsingManager: CollapsingManager,
    private val appLifecycleProvider: AppLifecycleProvider,
    navigationFactory: MediaNavigationDelegate.Factory,
) : ViewModel() {
    private val initialState = HomeUpcomingState()
    private val initialMode = filterManager.getFilter()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialMode)
    private val typeState = MutableStateFlow(initialState.type)
    private val collapse = CollapseDelegate(
        manager = collapsingManager,
        scope = viewModelScope,
        key = {
            when (filterState.value.mode) {
                MediaMode.Media -> CollapsingKey.HOME_MEDIA_UPCOMING
                MediaMode.Shows -> CollapsingKey.HOME_SHOWS_UPCOMING
                MediaMode.Movies -> CollapsingKey.HOME_MOVIES_UPCOMING
            }
        },
    )
    private val navigation = navigationFactory.create(
        scope = viewModelScope,
    )
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null

    init {
        loadData()

        observeUser()
        observeUpdates()
        observeFilter()
        observeForeground()
    }

    private fun observeForeground() {
        appLifecycleProvider.observeForegroundRefresh()
            .onEach { loadData(ignoreErrors = true) }
            .launchIn(viewModelScope)
    }

    private fun observeFilter() {
        filterManager.observeFilter()
            .distinctUntilChanged()
            .onEach { value ->
                filterState.update { value }
                collapse.refresh()
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeUser() {
        sessionManager.observeSessionChanges()
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    private fun observeUpdates() {
        dataChanges.observe(Kind.History, Kind.Watchlist)
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                typeState.update { getCalendarTypeUseCase.getType(CalendarSource.Personal) }

                val localItems = getUpcomingUseCase.getLocalUpcoming(
                    filter = filterState.value,
                    type = typeState.value,
                )

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { Done }
                } else {
                    loadingState.update { Loading }
                }

                itemsState.update {
                    delay(200.milliseconds) // Blinking workaround (Upcoming endpoint is HTTP-Cacheable)
                    getUpcomingUseCase.getUpcoming(
                        filter = filterState.value,
                        type = typeState.value,
                    )
                }

                ScheduleNotificationsWorker.schedule(appContext)
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

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            loadingState.update { Done }
            return true
        } else {
            loadingState.update { Idle }
        }

        return false
    }

    fun navigateToShow(show: Show) {
        navigation.toShow(show)
    }

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        navigation.toEpisode(show, episode)
    }

    fun navigateToMovie(movie: Movie) {
        navigation.toMovie(movie)
    }

    fun setType(type: ReleaseType) {
        if (type == typeState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getCalendarTypeUseCase.setType(type)
            loadData()
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapse.set(collapsed)
    }

    fun clearNavigation() {
        navigation.clear()
    }

    val state: StateFlow<HomeUpcomingState> = combine(
        loadingState,
        sessionManager.observeProfile(),
        itemsState,
        filterState,
        collapse.state,
        navigation.show,
        navigation.episode,
        navigation.movie,
        errorState,
        typeState,
    ) { state ->
        HomeUpcomingState(
            loading = state[0] as LoadingState,
            user = state[1] as User?,
            items = state[2] as ImmutableList<CalendarItem>?,
            filter = state[3] as GlobalFilter?,
            collapsed = state[4] as Boolean,
            navigateShow = state[5] as TraktId?,
            navigateEpisode = state[6] as Pair<TraktId, Episode>?,
            navigateMovie = state[7] as TraktId?,
            error = state[8] as Exception?,
            type = state[9] as ReleaseType,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
