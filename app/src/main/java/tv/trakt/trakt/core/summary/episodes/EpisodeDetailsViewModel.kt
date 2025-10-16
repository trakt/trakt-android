package tv.trakt.trakt.core.summary.episodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.episodes.navigation.EpisodeDetailsDestination
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeDetailsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeRatingsUseCase
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase
import tv.trakt.trakt.resources.R

@OptIn(FlowPreview::class)
internal class EpisodeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getEpisodeDetailsUseCase: GetEpisodeDetailsUseCase,
    private val getRatingsUseCase: GetEpisodeRatingsUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val showUpdatesSource: ShowDetailsUpdates,
    private val episodeUpdatesSource: EpisodeDetailsUpdates,
    private val sessionManager: SessionManager,
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
    private val episodeProgressState = MutableStateFlow(initialState.episodeProgress)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingProgress = MutableStateFlow(initialState.loadingProgress)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val userState = MutableStateFlow(initialState.user)

    init {
        loadUser()
        loadData()
        loadProgressData()
        observeData()
    }

    private fun observeData() {
        merge(
            showUpdatesSource.observeUpdates(Source.PROGRESS),
            showUpdatesSource.observeUpdates(Source.SEASONS),
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
                    Timber.w(error)
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
                    Timber.w(error)
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

                coroutineScope {
                    val progressAsync = async {
                        if (!loadProgressUseCase.isShowsLoaded()) {
                            loadProgressUseCase.loadShowsProgress()
                        }
                    }

                    progressAsync.await()
                }

                coroutineScope {
                    val progressAsync = async {
                        loadProgressUseCase.loadLocalShows()
                            .firstOrNull {
                                it.show.ids.trakt == showId
                            }?.seasons?.firstOrNull {
                                it.number == seasonEpisode.season
                            }?.episodes?.firstOrNull {
                                it.number == seasonEpisode.episode
                            }
                    }

                    val progress = progressAsync.await()

                    episodeProgressState.update {
                        EpisodeDetailsState.ProgressState(plays = progress?.plays)
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error)
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
                    Timber.w(error)
                }
            }
        }
    }

    fun removeFromWatched(playId: Long) {
        if (showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateEpisodeHistoryUseCase.removePlayFromHistory(playId)
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
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<EpisodeDetailsState> = combine(
        showState,
        episodeState,
        episodeRatingsState,
        episodeProgressState,
        loadingState,
        loadingProgress,
        infoState,
        errorState,
        userState,
    ) { state ->
        EpisodeDetailsState(
            show = state[0] as Show?,
            episode = state[1] as Episode?,
            episodeRatings = state[2] as ExternalRating?,
            episodeProgress = state[3] as EpisodeDetailsState.ProgressState?,
            loading = state[4] as LoadingState,
            loadingProgress = state[5] as LoadingState,
            info = state[6] as StringResource?,
            error = state[7] as Exception?,
            user = state[8] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
