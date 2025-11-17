@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.home.sections.activity.features.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager

internal class HomeSocialViewModel(
    private val getSocialActivityUseCase: GetSocialActivityUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
    private val modeManager: MediaModeManager,
) : ViewModel() {
    private val initialState = HomeSocialState()
    private val initialMode = modeManager.getMode()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialMode)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadData()
        observeUser()
        observeMode()
    }

    private fun observeUser() {
        viewModelScope.launch {
            userState.update { sessionManager.getProfile() }
            sessionManager.observeProfile()
                .drop(1)
                .distinctUntilChanged()
                .debounce(200)
                .collect { user ->
                    userState.update { user }
                    loadData()
                }
        }
    }

    private fun observeMode() {
        modeManager.observeMode()
            .distinctUntilChanged()
            .onEach { value ->
                filterState.update { value }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    fun loadData(ignoreErrors: Boolean = false) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val localItems = getSocialActivityUseCase.getLocalSocialActivity(
                    limit = HOME_SECTION_LIMIT,
                    filter = filterState.value,
                )

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getSocialActivityUseCase.getSocialActivity(
                        page = 1,
                        limit = HOME_SECTION_LIMIT,
                        filter = filterState.value,
                    )
                }
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

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            loadingState.update { DONE }
            return true
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

    fun clearNavigation() {
        navigateShow.update { null }
        navigateEpisode.update { null }
        navigateMovie.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<HomeSocialState> = combine(
        loadingState,
        itemsState,
        navigateShow,
        navigateEpisode,
        navigateMovie,
        errorState,
        userState,
    ) { states ->
        HomeSocialState(
            loading = states[0] as LoadingState,
            items = states[1] as? ImmutableList<HomeActivityItem>,
            navigateShow = states[2] as? TraktId,
            navigateEpisode = states[3] as? Pair<TraktId, Episode>,
            navigateMovie = states[4] as? TraktId,
            error = states[5] as? Exception,
            user = states[6] as? User,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
