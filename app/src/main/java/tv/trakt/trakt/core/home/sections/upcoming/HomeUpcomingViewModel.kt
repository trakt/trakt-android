package tv.trakt.trakt.core.home.sections.upcoming

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
import kotlinx.coroutines.flow.merge
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
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.home.sections.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

@Suppress("UNCHECKED_CAST")
@OptIn(FlowPreview::class)
internal class HomeUpcomingViewModel(
    private val appContext: Context,
    private val modeManager: MediaModeManager,
    private val getUpcomingUseCase: GetUpcomingUseCase,
    private val homeUpNextSource: HomeUpNextLocalDataSource,
    private val homeWatchlistSource: UserWatchlistLocalDataSource,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = HomeUpcomingState()
    private val initialMode = modeManager.getMode()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialMode)
    private val collapseState = MutableStateFlow(isCollapsed())
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var user: User? = null
    private var dataJob: Job? = null
    private var processingJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()
        observeUser()
        observeHome()
        observeMode()
    }

    private fun observeMode() {
        modeManager.observeMode()
            .distinctUntilChanged()
            .onEach { value ->
                filterState.update { value }
                collapseState.update { isCollapsed() }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeUser() {
        viewModelScope.launch {
            user = sessionManager.getProfile()
            sessionManager.observeProfile()
                .drop(1)
                .distinctUntilChanged()
                .debounce(200)
                .collect {
                    user = it
                    loadData()
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeHome() {
        merge(
            homeUpNextSource.observeUpdates(),
            homeWatchlistSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200)
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
                val localItems = getUpcomingUseCase.getLocalUpcoming(
                    filter = filterState.value,
                )

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    delay(200) // Blinking workaround (Upcoming endpoint is HTTP-Cacheable)
                    getUpcomingUseCase.getUpcoming(
                        filter = filterState.value,
                    )
                }

                ScheduleNotificationsWorker.schedule(appContext)
            } catch (error: Exception) {
                if (!ignoreErrors) {
                    errorState.update { error }
                }
                Timber.recordError(error)
            } finally {
                loadingState.update { DONE }
                dataJob = null
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<HomeUpcomingItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        } else {
            loadingState.update { IDLE }
        }

        return false
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
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            val key = when (filterState.value) {
                MediaMode.MEDIA -> CollapsingKey.HOME_MEDIA_UPCOMING
                MediaMode.SHOWS -> CollapsingKey.HOME_SHOWS_UPCOMING
                MediaMode.MOVIES -> CollapsingKey.HOME_MOVIES_UPCOMING
            }
            when {
                collapsed -> collapsingManager.collapse(key)
                else -> collapsingManager.expand(key)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(
            key = when (filterState.value) {
                MediaMode.MEDIA -> CollapsingKey.HOME_MEDIA_UPCOMING
                MediaMode.SHOWS -> CollapsingKey.HOME_SHOWS_UPCOMING
                MediaMode.MOVIES -> CollapsingKey.HOME_MOVIES_UPCOMING
            },
        )
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateEpisode.update { null }
        navigateMovie.update { null }
    }

    val state: StateFlow<HomeUpcomingState> = combine(
        loadingState,
        itemsState,
        filterState,
        collapseState,
        navigateShow,
        navigateEpisode,
        navigateMovie,
        errorState,
    ) { state ->
        HomeUpcomingState(
            loading = state[0] as LoadingState,
            items = state[1] as ImmutableList<HomeUpcomingItem>?,
            filter = state[2] as MediaMode?,
            collapsed = state[3] as Boolean,
            navigateShow = state[4] as TraktId?,
            navigateEpisode = state[5] as Pair<TraktId, Episode>?,
            navigateMovie = state[6] as TraktId?,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
