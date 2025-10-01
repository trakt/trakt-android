package tv.trakt.trakt.app.core.details.episode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.common.model.CastPerson
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState.HistoryState
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsState.StreamingsState
import tv.trakt.trakt.app.core.details.episode.navigation.EpisodeDestination
import tv.trakt.trakt.app.core.details.episode.usecases.GetCastCrewUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetCommentsUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetEpisodeDetailsUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetEpisodeHistoryUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetEpisodeSeasonUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetExternalRatingsUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.collection.ChangeHistoryUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.streamings.GetPlexUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.streamings.GetStreamingsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetRelatedShowsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.inappreview.usecases.RequestAppReviewUseCase
import tv.trakt.trakt.app.core.tutorials.TutorialsManager
import tv.trakt.trakt.app.core.tutorials.model.TutorialKey.WATCH_NOW_MORE
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.StaticStringResource
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.resources.R

internal class EpisodeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val tutorialsManager: TutorialsManager,
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getEpisodeDetailsUseCase: GetEpisodeDetailsUseCase,
    private val getExternalRatingsUseCase: GetExternalRatingsUseCase,
    private val getStreamingsUseCase: GetStreamingsUseCase,
    private val getPlexUseCase: GetPlexUseCase,
    private val getCastCrewUseCase: GetCastCrewUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val getRelatedShowsUseCase: GetRelatedShowsUseCase,
    private val getSeasonUseCase: GetEpisodeSeasonUseCase,
    private val getHistoryUseCase: GetEpisodeHistoryUseCase,
    private val changeHistoryUseCase: ChangeHistoryUseCase,
    private val appReviewUseCase: RequestAppReviewUseCase,
) : ViewModel() {
    private val initialState = EpisodeDetailsState()

    private val userState = MutableStateFlow(initialState.user)
    private val showDetailsState = MutableStateFlow(initialState.showDetails)
    private val episodeDetailsState = MutableStateFlow(initialState.episodeDetails)
    private val episodeRatingsState = MutableStateFlow(initialState.episodeRatings)
    private val episodeCastState = MutableStateFlow(initialState.episodeCast)
    private val episodeStreamingsState = MutableStateFlow(initialState.episodeStreamings)
    private val episodeRelatedState = MutableStateFlow(initialState.episodeRelated)
    private val episodeCommentsState = MutableStateFlow(initialState.episodeComments)
    private val episodeSeasonState = MutableStateFlow(initialState.episodeSeason)
    private val episodeHistoryState = MutableStateFlow(initialState.episodeHistory)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val reviewState = MutableStateFlow(initialState.isReviewRequest)
    private val snackMessageState = MutableStateFlow(initialState.snackMessage)

    private val destination = savedStateHandle.toRoute<EpisodeDestination>()

    init {
        loadData(
            showId = destination.showId.toTraktId(),
            episodeId = destination.episodeId.toTraktId(),
            seasonEpisode = SeasonEpisode(
                season = destination.season,
                episode = destination.episode,
            ),
        )
    }

    private fun loadData(
        showId: TraktId,
        episodeId: TraktId,
        seasonEpisode: SeasonEpisode,
    ) {
        viewModelScope.launch {
            try {
                val user = sessionManager.getProfile()

                val (show, episode) = coroutineScope {
                    val showAsync = async { getShowDetailsUseCase.getShowDetails(showId) }
                    val episodeAsync = async {
                        getEpisodeDetailsUseCase.getEpisodeDetails(
                            showId = showId,
                            episodeId = episodeId,
                            seasonEpisode = seasonEpisode,
                        )
                    }
                    return@coroutineScope Pair(
                        showAsync.await(),
                        episodeAsync.await(),
                    )
                }

                if (show != null && episode != null) {
                    userState.update { user }
                    showDetailsState.update { show }
                    episodeDetailsState.update { episode }

                    loadStreamings(show.ids, episode, user)
                    loadHistory(episode.ids.trakt)

                    loadExternalRatings(showId, episode.seasonEpisode)
                    loadCastCrew(showId, episode.seasonEpisode)
                    loadSeason(showId, episode.seasonEpisode)
                    loadComments(showId, episode.seasonEpisode)
                    loadRelatedShows(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading details: ${error.message}")
                }
            }
        }
    }

    private fun loadExternalRatings(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ) {
        viewModelScope.launch {
            try {
                val ratings = getExternalRatingsUseCase.getExternalRatings(
                    showId = showId,
                    seasonEpisode = seasonEpisode,
                )
                episodeRatingsState.update { ratings }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading external ratings: ${error.message}")
                }
            }
        }
    }

    private fun loadStreamings(
        showIds: Ids,
        episode: Episode,
        user: User?,
    ) {
        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated() || user == null) {
                    return@launch
                }
                episodeStreamingsState.update { it.copy(loading = true) }

                val plexService = getPlexUseCase.getPlexStatus(
                    showId = showIds.trakt,
                    episodeId = episode.ids.trakt,
                )
                if (plexService.isPlex) {
                    episodeStreamingsState.update {
                        it.copy(
                            plex = true,
                            slug = plexService.plexSlug,
                            loading = false,
                            info = when {
                                !tutorialsManager.get(WATCH_NOW_MORE) ->
                                    DynamicStringResource(R.string.button_text_long_press_for_more)
                                else -> null
                            },
                        )
                    }
                    return@launch
                }

                val streamingService = getStreamingsUseCase.getStreamingService(
                    user = user,
                    showId = showIds.trakt,
                    episode = episode,
                )

                episodeStreamingsState.update {
                    it.copy(
                        slug = showIds.plex,
                        loading = false,
                        service = streamingService.streamingService,
                        noServices = streamingService.noServices,
                        info = when {
                            !tutorialsManager.get(WATCH_NOW_MORE) ->
                                DynamicStringResource(R.string.button_text_long_press_for_more)
                            else -> null
                        },
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading streaming services: ${error.message}")
                    episodeStreamingsState.update { StreamingsState() }
                }
            }
        }
    }

    private fun loadCastCrew(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ) {
        viewModelScope.launch {
            try {
                val showCast = getCastCrewUseCase.getCastCrew(showId, seasonEpisode)
                episodeCastState.update { showCast }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading cast and crew: ${error.message}")
                }
            }
        }
    }

    private fun loadSeason(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ) {
        viewModelScope.launch {
            try {
                val episodes = getSeasonUseCase.getEpisodeSeason(showId, seasonEpisode)
                episodeSeasonState.update { episodes }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading season: ${error.message}")
                }
            }
        }
    }

    private fun loadHistory(episodeId: TraktId) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                episodeHistoryState.update {
                    HistoryState(
                        isLoading = true,
                        episodes = it.episodes,
                    )
                }

                val episodes = getHistoryUseCase.getEpisodeHistory(episodeId, null)

                episodeHistoryState.update {
                    HistoryState(
                        isLoading = false,
                        episodes = episodes,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    episodeHistoryState.update { HistoryState() }
                    Timber.e("Error loading history: ${error.message}")
                }
            }
        }
    }

    private fun loadComments(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ) {
        viewModelScope.launch {
            try {
                val comments = getCommentsUseCase.getComments(showId, seasonEpisode)
                episodeCommentsState.update { comments }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e(error, "Error loading comments: ${error.message}")
                }
            }
        }
    }

    private fun loadRelatedShows(showId: TraktId) {
        viewModelScope.launch {
            try {
                val relatedShows = getRelatedShowsUseCase.getRelatedShows(showId)
                episodeRelatedState.update { relatedShows }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading related shows: ${error.message}")
                }
            }
        }
    }

    fun addToHistory() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated() || episodeHistoryState.value.isLoading) {
                return@launch
            }
            try {
                episodeHistoryState.update { it.copy(isLoading = true) }
                val episodeId = destination.episodeId.toTraktId()

                changeHistoryUseCase.addToHistory(episodeId)
                val episodes = getHistoryUseCase.getEpisodeHistory(episodeId, nowUtc())

                episodeHistoryState.update {
                    it.copy(
                        isLoading = false,
                        episodes = episodes,
                    )
                }

                requestReviewIfNeeded()
                showSnackMessage(DynamicStringResource(R.string.text_info_history_added))
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error adding history: ${error.message}")
                }
            }
        }
    }

    fun removeFromHistory(episodePlayId: Long) {
        viewModelScope.launch {
            if (episodePlayId <= 0 || !sessionManager.isAuthenticated() || episodeHistoryState.value.isLoading) {
                return@launch
            }
            try {
                episodeHistoryState.update { it.copy(isLoading = true) }
                val episodeId = destination.episodeId.toTraktId()

                changeHistoryUseCase.removeFromHistory(episodePlayId)
                val episodes = getHistoryUseCase.getEpisodeHistory(episodeId, nowUtc())

                episodeHistoryState.update {
                    it.copy(
                        isLoading = false,
                        episodes = episodes,
                    )
                }

                showSnackMessage(DynamicStringResource(R.string.text_info_history_removed))
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error removing history: ${error.message}")
                }
            }
        }
    }

    private suspend fun requestReviewIfNeeded() {
        appReviewUseCase.incrementCount()
        reviewState.update {
            appReviewUseCase.shouldRequest()
        }
    }

    private fun showSnackMessage(message: StringResource) {
        snackMessageState.update { message }
    }

    fun clearWatchNowTip() {
        viewModelScope.launch {
            tutorialsManager.acknowledge(WATCH_NOW_MORE)
            episodeStreamingsState.update { it.copy(info = null) }
        }
    }

    fun clearInfoMessage() {
        snackMessageState.update { null }
    }

    fun clearReviewRequest() {
        reviewState.update { false }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<EpisodeDetailsState> = combine(
        userState,
        showDetailsState,
        episodeDetailsState,
        episodeRatingsState,
        episodeStreamingsState,
        episodeCastState,
        episodeRelatedState,
        episodeCommentsState,
        episodeSeasonState,
        episodeHistoryState,
        loadingState,
        snackMessageState,
        reviewState,
    ) { states ->
        EpisodeDetailsState(
            user = states[0] as User?,
            showDetails = states[1] as Show?,
            episodeDetails = states[2] as Episode?,
            episodeRatings = states[3] as ExternalRating?,
            episodeStreamings = states[4] as StreamingsState,
            episodeCast = states[5] as ImmutableList<CastPerson>?,
            episodeRelated = states[6] as ImmutableList<Show>?,
            episodeComments = states[7] as ImmutableList<Comment>?,
            episodeSeason = states[8] as ImmutableList<Episode>?,
            episodeHistory = states[9] as HistoryState,
            isLoading = states[10] as Boolean,
            snackMessage = states[11] as StringResource?,
            isReviewRequest = states[12] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
