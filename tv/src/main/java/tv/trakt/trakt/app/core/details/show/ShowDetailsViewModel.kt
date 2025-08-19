package tv.trakt.trakt.app.core.details.show

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
import tv.trakt.trakt.app.common.model.CustomList
import tv.trakt.trakt.app.common.model.ExternalRating
import tv.trakt.trakt.app.common.model.ExtraVideo
import tv.trakt.trakt.app.core.details.show.ShowDetailsState.CollectionState
import tv.trakt.trakt.app.core.details.show.ShowDetailsState.StreamingsState
import tv.trakt.trakt.app.core.details.show.models.ShowSeasons
import tv.trakt.trakt.app.core.details.show.navigation.ShowDestination
import tv.trakt.trakt.app.core.details.show.usecases.GetCastCrewUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetCommentsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetCustomListsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetExternalRatingsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetExtraVideosUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetRelatedShowsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetShowSeasonsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetStreamingsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.collection.ChangeHistoryUseCase
import tv.trakt.trakt.app.core.details.show.usecases.collection.ChangeWatchlistUseCase
import tv.trakt.trakt.app.core.details.show.usecases.collection.GetCollectionUseCase
import tv.trakt.trakt.app.core.episodes.model.Season
import tv.trakt.trakt.app.core.tutorials.TutorialsManager
import tv.trakt.trakt.app.core.tutorials.model.TutorialKey.WATCH_NOW_MORE
import tv.trakt.trakt.app.helpers.DynamicStringResource
import tv.trakt.trakt.app.helpers.StaticStringResource
import tv.trakt.trakt.app.helpers.StringResource
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.resources.R

internal class ShowDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDetailsUseCase: GetShowDetailsUseCase,
    private val getExternalRatingsUseCase: GetExternalRatingsUseCase,
    private val getExtraVideosUseCase: GetExtraVideosUseCase,
    private val getCastCrewUseCase: GetCastCrewUseCase,
    private val getRelatedShowsUseCase: GetRelatedShowsUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val getStreamingsUseCase: GetStreamingsUseCase,
    private val getListsUseCase: GetCustomListsUseCase,
    private val getSeasonsUseCase: GetShowSeasonsUseCase,
    private val getCollectionUseCase: GetCollectionUseCase,
    private val historyUseCase: ChangeHistoryUseCase,
    private val watchlistUseCase: ChangeWatchlistUseCase,
    private val sessionManager: SessionManager,
    private val tutorialsManager: TutorialsManager,
) : ViewModel() {
    private val initialState = ShowDetailsState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val showDetailsState = MutableStateFlow(initialState.showDetails)
    private val showRatingsState = MutableStateFlow(initialState.showRatings)
    private val showVideosState = MutableStateFlow(initialState.showVideos)
    private val showCastState = MutableStateFlow(initialState.showCast)
    private val showRelatedState = MutableStateFlow(initialState.showRelated)
    private val showCommentsState = MutableStateFlow(initialState.showComments)
    private val showListsState = MutableStateFlow(initialState.showLists)
    private val showStreamingsState = MutableStateFlow(initialState.showStreamings)
    private val showSeasonsState = MutableStateFlow(initialState.showSeasons)
    private val showCollectionState = MutableStateFlow(initialState.showCollection)
    private val userState = MutableStateFlow(initialState.user)
    private val snackMessageState = MutableStateFlow(initialState.snackMessage)

    private val show = savedStateHandle.toRoute<ShowDestination>()
    private var loadingJob: Job? = null

    init {
        loadData(show.showId.toTraktId())
    }

    private fun loadData(showId: TraktId) {
        viewModelScope.launch {
            try {
                val user = sessionManager.getProfile()
                val show = getDetailsUseCase.getShowDetails(showId)
                show?.let {
                    userState.update { user }
                    showDetailsState.update { show }

                    loadCollection(showId)
                    loadStreamings(it.ids, user)

                    loadExternalRatings(showId)
                    loadExtraVideos(showId)
                    loadCastCrew(showId)
                    loadSeasons(showId)
                    loadComments(showId)
                    loadRelatedShows(showId)
                    loadLists(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading show details: ${error.message}")
                }
            }
        }
    }

    private fun loadExternalRatings(showId: TraktId) {
        viewModelScope.launch {
            try {
                showRatingsState.update {
                    getExternalRatingsUseCase.getExternalRatings(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading external ratings: ${error.message}")
                }
            }
        }
    }

    private fun loadExtraVideos(showId: TraktId) {
        viewModelScope.launch {
            try {
                showVideosState.update {
                    getExtraVideosUseCase.getExtraVideos(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading extra videos: ${error.message}")
                }
            }
        }
    }

    private fun loadCastCrew(showId: TraktId) {
        viewModelScope.launch {
            try {
                showCastState.update {
                    getCastCrewUseCase.getCastCrew(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading cast and crew: ${error.message}")
                }
            }
        }
    }

    private fun loadSeasons(showId: TraktId) {
        viewModelScope.launch {
            if (showSeasonsState.value.isSeasonLoading) {
                return@launch
            }
            try {
                showSeasonsState.update {
                    getSeasonsUseCase.getAllSeasons(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading seasons: ${error.message}")
                }
            }
        }
    }

    fun loadSeason(season: Season) {
        loadingJob = viewModelScope.launch {
            delay(250)
            showSeasonsState.update {
                it.copy(isSeasonLoading = true)
            }
        }

        viewModelScope.launch {
            if (showSeasonsState.value.isSeasonLoading ||
                season.number == showSeasonsState.value.selectedSeason?.number
            ) {
                loadingJob?.cancel()
                return@launch
            }

            try {
                showSeasonsState.update {
                    it.copy(selectedSeason = season)
                }

                val episodes = getSeasonsUseCase.getSeason(
                    showId = show.showId.toTraktId(),
                    season = season.number,
                )

                showSeasonsState.update {
                    it.copy(
                        selectedSeason = season,
                        selectedSeasonEpisodes = episodes,
                        isSeasonLoading = false,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading season: ${error.message}")
                    showSeasonsState.update { it.copy(isSeasonLoading = false) }
                }
            } finally {
                loadingJob?.cancel()
            }
        }
    }

    private fun loadRelatedShows(showId: TraktId) {
        viewModelScope.launch {
            try {
                showRelatedState.update {
                    getRelatedShowsUseCase.getRelatedShows(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading related shows: ${error.message}")
                }
            }
        }
    }

    private fun loadComments(showId: TraktId) {
        viewModelScope.launch {
            try {
                showCommentsState.update {
                    getCommentsUseCase.getComments(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading comments: ${error.message}")
                }
            }
        }
    }

    private fun loadLists(showId: TraktId) {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val officialListsAsync = async { getListsUseCase.getOfficialLists(showId) }
                    val personalListsAsync = async { getListsUseCase.getPersonalLists(showId) }

                    showListsState.update {
                        (officialListsAsync.await() + personalListsAsync.await())
                            .toImmutableList()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e(error, "Error loading lists: ${error.message}")
                }
            }
        }
    }

    private fun loadStreamings(
        showIds: Ids,
        user: User?,
    ) {
        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated() || user == null) {
                    return@launch
                }
                showStreamingsState.update { it.copy(loading = true) }

                val streamingService = getStreamingsUseCase.getStreamingService(
                    user = user,
                    showId = showIds.trakt,
                )

                showStreamingsState.update {
                    it.copy(
                        slug = showIds.plex,
                        loading = false,
                        service = streamingService.streamingService,
                        noServices = streamingService.noServices,
                        info = when {
                            !tutorialsManager.get(WATCH_NOW_MORE) ->
                                DynamicStringResource(R.string.info_watchnow_long_press)
                            else -> null
                        },
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    showStreamingsState.update { StreamingsState() }
                    Timber.e("Error loading streaming services: ${error.message}")
                }
            }
        }
    }

    private fun loadCollection(showId: TraktId) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                showCollectionState.update {
                    it.copy(
                        isWatchedLoading = true,
                        isWatchlistLoading = true,
                    )
                }

                coroutineScope {
                    val watchedAsync = async { getCollectionUseCase.getWatchedShow(showId) }
                    val watchlistAsync = async { getCollectionUseCase.getWatchlistShow(showId) }

                    val watched = watchedAsync.await()
                    val watchlist = watchlistAsync.await()

                    showCollectionState.update {
                        it.copy(
                            isWatchedLoading = false,
                            isWatchlistLoading = false,
                            isWatchlist = watchlist != null,
                            isWatched = watched != null,
                            episodesPlays = watched?.episodesPlays ?: 0,
                            episodesAiredCount = watched?.episodesAiredCount ?: 0,
                        )
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error loading collection: ${error.message}")
                    showCollectionState.update { CollectionState() }
                }
            }
        }
    }

    fun toggleHistory() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated() || showCollectionState.value.isWatchedLoading) {
                return@launch
            }
            try {
                showCollectionState.update { it.copy(isWatchedLoading = true) }

                val episodesPlays = historyUseCase.addToHistory(
                    showId = show.showId.toTraktId(),
                    episodesPlays = showCollectionState.value.episodesPlays,
                    episodesAiredCount = showCollectionState.value.episodesAiredCount,
                )

                showCollectionState.update {
                    it.copy(
                        isWatchlist = false,
                        isWatched = true,
                        isWatchedLoading = false,
                        episodesPlays = episodesPlays,
                        episodesAiredCount = it.episodesAiredCount,
                    )
                }

                showSnackMessage(DynamicStringResource(R.string.info_history_added))
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error toggling history: ${error.message}")
                }
            }
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                showCollectionState.update { it.copy(isWatchlistLoading = true) }

                if (showCollectionState.value.isWatchlist) {
                    watchlistUseCase.removeFromWatchlist(show.showId.toTraktId())
                    showCollectionState.update {
                        it.copy(isWatchlist = false, isWatchlistLoading = false)
                    }
                    showSnackMessage(DynamicStringResource(R.string.info_watchlist_removed))
                } else {
                    watchlistUseCase.addToWatchlist(show.showId.toTraktId())
                    showCollectionState.update {
                        it.copy(isWatchlist = true, isWatchlistLoading = false)
                    }
                    showSnackMessage(DynamicStringResource(R.string.info_watchlist_added))
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    showSnackMessage(StaticStringResource(error.toString()))
                    Timber.e("Error toggling watchlist: ${error.message}")
                }
            }
        }
    }

    private fun showSnackMessage(message: StringResource) {
        snackMessageState.update { message }
    }

    fun clearWatchNowTip() {
        viewModelScope.launch {
            tutorialsManager.acknowledge(WATCH_NOW_MORE)
            showStreamingsState.update { it.copy(info = null) }
        }
    }

    fun clearInfoMessage() {
        snackMessageState.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowDetailsState> = combine(
        loadingState,
        showDetailsState,
        showRatingsState,
        showVideosState,
        showCastState,
        showRelatedState,
        showCommentsState,
        showListsState,
        showStreamingsState,
        showCollectionState,
        showSeasonsState,
        userState,
        snackMessageState,
    ) { states ->
        ShowDetailsState(
            isLoading = states[0] as Boolean,
            showDetails = states[1] as Show?,
            showRatings = states[2] as ExternalRating?,
            showVideos = states[3] as ImmutableList<ExtraVideo>?,
            showCast = states[4] as ImmutableList<CastPerson>?,
            showRelated = states[5] as ImmutableList<Show>?,
            showComments = states[6] as ImmutableList<Comment>?,
            showLists = states[7] as ImmutableList<CustomList>?,
            showStreamings = states[8] as StreamingsState,
            showCollection = states[9] as CollectionState,
            showSeasons = states[10] as ShowSeasons,
            user = states[11] as User?,
            snackMessage = states[12] as StringResource?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
