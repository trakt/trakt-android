package tv.trakt.trakt.core.summary.episodes

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.MediaType.EPISODE
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker
import tv.trakt.trakt.core.summary.episodes.EpisodeDetailsState.UserRatingsState
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.SEASON
import tv.trakt.trakt.core.summary.episodes.features.actors.usecases.GetEpisodeDirectorUseCase
import tv.trakt.trakt.core.summary.episodes.navigation.EpisodeDetailsDestination
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeDetailsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeRatingsUseCase
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class EpisodeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val appContext: Context,
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getEpisodeDetailsUseCase: GetEpisodeDetailsUseCase,
    private val getEpisodeDirectorUseCase: GetEpisodeDirectorUseCase,
    private val getRatingsUseCase: GetEpisodeRatingsUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadRatingUseCase: LoadUserRatingsUseCase,
    private val updateHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val showUpdatesSource: ShowDetailsUpdates,
    private val episodeUpdatesSource: EpisodeDetailsUpdates,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = EpisodeDetailsState()
    private val destination = savedStateHandle.toRoute<EpisodeDetailsDestination>()

    private val showId = destination.showId.toTraktId()
    private val episodeId = destination.episodeId.toTraktId()
    private val seasonEpisode = SeasonEpisode(
        season = destination.season,
        episode = destination.episode,
    )

    private val showState = MutableStateFlow(initialState.show)
    private val episodeState = MutableStateFlow(initialState.episode)
    private val episodeRatingsState = MutableStateFlow(initialState.episodeRatings)
    private val episodeUserRatingsState = MutableStateFlow(initialState.episodeUserRating)
    private val episodeProgressState = MutableStateFlow(initialState.episodeProgress)
    private val episodeCreatorState = MutableStateFlow(initialState.episodeCreator)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingProgress = MutableStateFlow(initialState.loadingProgress)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val userState = MutableStateFlow(initialState.user)
    private val metaCollapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.EPISODE_META))

    private var ratingJob: kotlinx.coroutines.Job? = null
    private var metaCollapseJob: kotlinx.coroutines.Job? = null

    init {
        loadUser()
        loadData()
        loadProgressData()
        loadUserRatingData()
        loadCreator()

        observeData()

        analytics.logScreenView(
            screenName = "episode_details",
        )
    }

    private fun observeData() {
        merge(
            showUpdatesSource.observeUpdates(Source.PROGRESS),
            showUpdatesSource.observeUpdates(Source.SEASONS),
            episodeUpdatesSource.observeUpdates(SEASON),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadProgressData(
                    ignoreErrors = true,
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val showAsync = async { getShowDetailsUseCase.getLocalShow(showId) }
                val episodeAsync = async { getEpisodeDetailsUseCase.getLocalEpisode(episodeId) }

                var show = showAsync.await()
                var episode = episodeAsync.await()

                if (episode == null || show == null) {
                    loadingState.update { LOADING }
                }

                if (show == null) {
                    show = getShowDetailsUseCase.getShow(
                        showId = showId,
                    )
                }
                showState.update { show }

                if (episode == null) {
                    episode = getEpisodeDetailsUseCase.getEpisode(
                        showId = showId,
                        seasonEpisode = seasonEpisode,
                    )
                }
                episodeState.update { episode }

                loadRatings(episode)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private fun loadProgressData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                if (!loadProgressUseCase.isShowsLoaded()) {
                    loadProgressUseCase.loadShowsProgress()
                }

                val progress = loadProgressUseCase.loadLocalShows()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }?.seasons?.firstOrNull {
                        it.number == seasonEpisode.season
                    }?.episodes?.firstOrNull {
                        it.number == seasonEpisode.episode
                    }

                episodeProgressState.update {
                    EpisodeDetailsState.ProgressState(plays = progress?.plays)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    private fun loadRatings(episode: Episode?) {
        if (episode?.firstAired?.isNowOrBefore() != true) {
            // Don't load ratings for unreleased episodes
            return
        }
        viewModelScope.launch {
            try {
                episodeRatingsState.update {
                    getRatingsUseCase.getExternalRatings(
                        showId = showId,
                        season = episode.season,
                        episode = episode.number,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadUserRatingData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                episodeUserRatingsState.update {
                    UserRatingsState(
                        loading = LOADING,
                    )
                }

                if (!loadRatingUseCase.isEpisodesLoaded()) {
                    loadRatingUseCase.loadEpisodes()
                }

                val userRatings = loadRatingUseCase.loadLocalEpisodes()
                val userRating = userRatings[episodeId]

                episodeUserRatingsState.update {
                    UserRatingsState(
                        rating = userRating,
                        loading = DONE,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadCreator() {
        viewModelScope.launch {
            try {
                episodeCreatorState.update {
                    getEpisodeDirectorUseCase.getDirector(
                        showId = showId,
                        seasonEpisode = seasonEpisode,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun addToWatched(customDate: DateSelectionResult? = null) {
        if (isLoading()) {
            return
        }
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateHistoryUseCase.addToHistory(
                    episodeId = episodeId,
                    customDate = customDate,
                )
                val progress = loadProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }?.seasons?.firstOrNull {
                        it.number == seasonEpisode.season
                    }?.episodes?.firstOrNull {
                        it.number == seasonEpisode.episode
                    }

                episodeProgressState.update { state ->
                    state?.copy(plays = progress?.plays)
                }

                episodeUpdatesSource.notifyUpdate(PROGRESS)

                infoState.update { DynamicStringResource(R.string.text_info_history_added) }
                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "episode_details",
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.d(error, "Failed to add movie to history")
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    fun removeFromWatched() {
        if (isLoading()) {
            return
        }
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateHistoryUseCase.removeEpisodeFromHistory(episodeId.value)
                loadProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }?.seasons?.firstOrNull {
                        it.number == seasonEpisode.season
                    }?.episodes?.firstOrNull {
                        it.number == seasonEpisode.episode
                    }

                episodeProgressState.update { state ->
                    state?.copy(plays = 0)
                }

                episodeUpdatesSource.notifyUpdate(PROGRESS)
                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "episode",
                    source = "episode_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    fun removeFromWatched(playId: Long) {
        if (isLoading()) {
            return
        }
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateHistoryUseCase.removePlayFromHistory(playId)
                val progress = loadProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }?.seasons?.firstOrNull {
                        it.number == seasonEpisode.season
                    }?.episodes?.firstOrNull {
                        it.number == seasonEpisode.episode
                    }

                episodeProgressState.update { state ->
                    state?.copy(plays = progress?.plays)
                }

                episodeUpdatesSource.notifyUpdate(PROGRESS)
                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "episode",
                    source = "episode_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    fun navigateToEpisode(episode: Episode) {
        if (isLoading()) {
            return
        }
        if (episode.ids.trakt == episodeId) {
            // Same episode, don't navigate
            return
        }
        viewModelScope.launch {
            episodeLocalDataSource.upsertEpisodes(listOf(episode))
            navigateEpisode.update {
                Pair(showId, episode)
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    fun clearNavigation() {
        navigateEpisode.update { null }
    }

    fun addRating(newRating: Int) {
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            episodeUserRatingsState.update {
                UserRatingsState(
                    rating = UserRating(
                        mediaId = episodeId,
                        mediaType = EPISODE,
                        rating = newRating,
                    ),
                    loading = DONE,
                )
            }

            // Debounce to avoid multiple rapid calls.
            delay(2.seconds)
            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = episodeId,
                mediaType = EPISODE,
                rating = newRating,
            )
        }
    }

    fun setMetaCollapsed(collapsed: Boolean) {
        metaCollapseState.update { collapsed }
        metaCollapseJob?.cancel()
        metaCollapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.EPISODE_META)
                else -> collapsingManager.expand(CollapsingKey.EPISODE_META)
            }
        }
    }

    private fun isLoading(): Boolean {
        return showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        showState,
        episodeState,
        episodeRatingsState,
        episodeUserRatingsState,
        episodeProgressState,
        episodeCreatorState,
        loadingState,
        loadingProgress,
        infoState,
        errorState,
        userState,
        navigateEpisode,
        metaCollapseState,
    ) { state ->
        EpisodeDetailsState(
            show = state[0] as Show?,
            episode = state[1] as Episode?,
            episodeRatings = state[2] as ExternalRating?,
            episodeUserRating = state[3] as UserRatingsState?,
            episodeProgress = state[4] as EpisodeDetailsState.ProgressState?,
            episodeCreator = state[5] as Person?,
            loading = state[6] as LoadingState,
            loadingProgress = state[7] as LoadingState,
            info = state[8] as StringResource?,
            error = state[9] as Exception?,
            user = state[10] as User?,
            navigateEpisode = state[11] as Pair<TraktId, Episode>?,
            metaCollapsed = state[12] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
