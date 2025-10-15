package tv.trakt.trakt.core.summary.episodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
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
import tv.trakt.trakt.core.summary.episodes.navigation.EpisodeDetailsDestination
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeDetailsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase

@OptIn(FlowPreview::class)
internal class EpisodeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getDetailsUseCase: GetEpisodeDetailsUseCase,
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
                var episode = getDetailsUseCase.getLocalEpisode(episodeId)
                var show = getShowDetailsUseCase.getLocalShow(showId)

                if (episode == null || show == null) {
                    loadingState.update { LOADING }
                }

                if (episode == null) {
                    episode = getDetailsUseCase.getEpisode(
                        showId = showId,
                        seasonEpisode = seasonEpisode,
                    )
                }
                if (show == null) {
                    show = getShowDetailsUseCase.getShow(
                        showId = showId,
                    )
                }

                episodeState.update { episode }
                showState.update { show }

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

    private fun loadRatings(episode: Episode?) {
        if (episode?.firstAired?.isNowOrBefore() != true) {
            // Don't load ratings for unreleased episodes
            return
        }
//        viewModelScope.launch {
//            try {
//                episodeRatingsState.update {
//                    getExternalRatingsUseCase.getExternalRatings(episodeId)
//                }
//            } catch (error: Exception) {
//                error.rethrowCancellation {
//                    Timber.w(error)
//                }
//            }
//        }
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
