package tv.trakt.trakt.core.summary.shows.features.seasons.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.isNotNull
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.AllSeasons
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.Progress
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.Seasons
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.WatchedUntil
import tv.trakt.trakt.core.summary.shows.features.seasons.all.navigation.AllShowSeasonsDestination
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons.Helpers.markWatchedEpisodes
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons.Helpers.markWatchedSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.usecases.GetShowSeasonsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
internal class AllShowSeasonsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getSeasonsUseCase: GetShowSeasonsUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val showDetailsUpdates: ShowDetailsUpdates,
    private val episodeDetailsUpdates: EpisodeDetailsUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = AllShowSeasonsState()

    private val destination = savedStateHandle.toRoute<AllShowSeasonsDestination>()
    private val showId = destination.showId.toTraktId()

    private val backgroundState = MutableStateFlow(destination.backgroundUrl)
    private val userState = MutableStateFlow(initialState.user)
    private val showState = MutableStateFlow(initialState.show)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingEpisodeState = MutableStateFlow(initialState.loadingEpisode)
    private val loadingSeasonState = MutableStateFlow(initialState.loadingSeason)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        merge(
            showDetailsUpdates.observeUpdates(Progress),
            showDetailsUpdates.observeUpdates(WatchedUntil),
            episodeDetailsUpdates.observeUpdates(Source.Progress),
            episodeDetailsUpdates.observeUpdates(Source.Season),
            episodeDetailsUpdates.observeUpdates(Source.History),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                coroutineScope {
                    val userAsync = async { sessionManager.getProfile() }
                    val showAsync = async {
                        getShowDetailsUseCase.getLocalShow(showId)
                            ?: getShowDetailsUseCase.getShow(showId)
                    }

                    userState.update { userAsync.await() }
                    showState.update { showAsync.await() }
                }

                showState.update {
                    getShowDetailsUseCase.getLocalShow(showId)
                        ?: getShowDetailsUseCase.getShow(showId)
                }

                val watched = when {
                    userState.isNotNull() -> {
                        val isLoaded = loadUserProgressUseCase.isShowsLoaded()
                        when {
                            isLoaded -> loadUserProgressUseCase.loadLocalShows()
                            else -> loadUserProgressUseCase.loadShowsProgress()
                        }.firstOrNull {
                            it.showId == showId
                        }
                    }
                    else -> {
                        null
                    }
                }

                val seasons = getSeasonsUseCase.getAllSeasons(
                    showId = showId,
                    initialSeason = destination.initialSeason ?: 1,
                )

                itemsState.update {
                    seasons.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = seasons.seasons,
                            progress = watched?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = seasons.selectedSeasonEpisodes,
                            progress = watched?.seasons,
                            checkable = userState.value != null,
                        ),
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
                loadingState.update { Done }
            }
        }
    }

    fun loadSeason(season: SeasonItem) {
        if (
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading ||
            itemsState.value.isSeasonLoading ||
            season.season.number == itemsState.value.selectedSeason?.number
        ) {
            return
        }

        viewModelScope.launch {
            try {
                itemsState.update {
                    it.copy(
                        isSeasonLoading = true,
                        selectedSeason = season.season,
                    )
                }

                val progress = when {
                    userState.isNotNull() -> when {
                        loadUserProgressUseCase.isShowsLoaded() -> loadUserProgressUseCase.loadLocalShows()
                        else -> loadUserProgressUseCase.loadShowsProgress()
                    }.firstOrNull { it.showId == showId }

                    else -> null
                }

                val episodes = getSeasonsUseCase.getSeasonEpisodes(
                    showId = showId,
                    season = season.season.number,
                )

                itemsState.update {
                    it.copy(
                        selectedSeason = season.season,
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = episodes,
                            progress = progress?.seasons,
                            checkable = userState.isNotNull(),
                        ),
                        isSeasonLoading = false,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                    itemsState.update {
                        it.copy(isSeasonLoading = false)
                    }
                }
            }
        }
    }

    fun addToWatched(
        episode: Episode,
        customDate: DateSelectionResult? = null,
    ) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) return@launch

            try {
                loadingEpisodeState.update { Loading }
                setLoadingEpisode(episode)

                updateEpisodeHistoryUseCase.addToHistory(
                    episodeId = episode.ids.trakt,
                    customDate = customDate,
                )
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull { it.showId == showId }

                itemsState.update {
                    it.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = it.seasons,
                            progress = progress?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(Seasons)
                showDetailsUpdates.notifyUpdate(AllSeasons)

                infoState.update { DynamicStringResource(R.string.text_info_history_added) }
                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "all_show_seasons_screen",
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingEpisodeState.update { Done }
            }
        }
    }

    fun addToWatched(
        season: ShowSeasons,
        customDate: DateSelectionResult? = null,
    ) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) return@launch

            try {
                loadingSeasonState.update { Loading }

                val episodesToAdd = season.selectedSeasonEpisodes
                    .filter { !it.isWatched && it.episode.isReleased }
                    .map { it.episode.ids.trakt }

                updateEpisodeHistoryUseCase.addToHistory(
                    episodeIds = episodesToAdd,
                    customDate = customDate,
                )
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull { it.showId == showId }

                itemsState.update {
                    it.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = it.seasons,
                            progress = progress?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(Seasons)
                showDetailsUpdates.notifyUpdate(AllSeasons)

                infoState.update { DynamicStringResource(R.string.text_info_history_added) }
                analytics.progress.logAddWatchedMedia(
                    mediaType = "season",
                    source = "all_show_seasons_screen",
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingSeasonState.update { Done }
            }
        }
    }

    fun removeFromWatched(episode: Episode) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) return@launch

            try {
                loadingEpisodeState.update { Loading }
                setLoadingEpisode(episode)

                updateEpisodeHistoryUseCase.removeEpisodeFromHistory(episode.ids.trakt.value)
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull { it.showId == showId }

                itemsState.update {
                    it.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = it.seasons,
                            progress = progress?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(Seasons)
                showDetailsUpdates.notifyUpdate(AllSeasons)

                infoState.update { DynamicStringResource(R.string.text_info_history_removed) }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "episode",
                    source = "all_show_seasons_screen",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingEpisodeState.update { Done }
            }
        }
    }

    fun removeFromWatched(season: Season) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) return@launch

            try {
                loadingSeasonState.update { Loading }

                updateEpisodeHistoryUseCase.removeSeasonFromHistory(season.ids.trakt.value)
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull { it.showId == showId }

                itemsState.update {
                    it.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = it.seasons,
                            progress = progress?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(Seasons)
                showDetailsUpdates.notifyUpdate(AllSeasons)

                infoState.update { DynamicStringResource(R.string.text_info_history_removed) }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "season",
                    source = "all_show_seasons_screen",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingSeasonState.update { Done }
            }
        }
    }

    fun navigateToEpisode(episode: Episode) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            episodeLocalDataSource.upsertEpisodes(listOf(episode))
            navigateEpisode.update {
                Pair(showId, episode)
            }
        }
    }

    private suspend fun setLoadingEpisode(episode: Episode) {
        itemsState.update {
            it.copy(
                selectedSeasonEpisodes = it.selectedSeasonEpisodes
                    .asyncMap { e ->
                        e.copy(isLoading = episode.ids.trakt == e.episode.ids.trakt)
                    }.toImmutableList(),
            )
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    fun clearNavigation() {
        navigateEpisode.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        showState,
        userState,
        backgroundState,
        itemsState,
        loadingState,
        loadingEpisodeState,
        loadingSeasonState,
        navigateEpisode,
        infoState,
        errorState,
    ) { state ->
        AllShowSeasonsState(
            show = state[0] as Show?,
            user = state[1] as User?,
            backgroundUrl = state[2] as String?,
            items = state[3] as ShowSeasons,
            loading = state[4] as LoadingState,
            loadingEpisode = state[5] as LoadingState,
            loadingSeason = state[6] as LoadingState,
            navigateEpisode = state[7] as Pair<TraktId, Episode>?,
            info = state[8] as StringResource?,
            error = state[9] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AllShowSeasonsState(backgroundUrl = destination.backgroundUrl),
    )
}
