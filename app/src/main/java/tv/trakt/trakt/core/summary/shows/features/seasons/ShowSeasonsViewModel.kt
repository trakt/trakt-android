package tv.trakt.trakt.core.summary.shows.features.seasons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
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
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.SEASONS
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.usecases.GetShowSeasonsUseCase
import tv.trakt.trakt.core.sync.model.ProgressItem
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.resources.R

@OptIn(FlowPreview::class)
internal class ShowSeasonsViewModel(
    private val show: Show,
    private val getSeasonsUseCase: GetShowSeasonsUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val showDetailsUpdates: ShowDetailsUpdates,
    private val episodeDetailsUpdates: EpisodeDetailsUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ShowSeasonsState()

    private val showState = MutableStateFlow(show)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingEpisodeState = MutableStateFlow(initialState.loadingEpisode)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        merge(
            showDetailsUpdates.observeUpdates(PROGRESS),
            episodeDetailsUpdates.observeUpdates(Source.PROGRESS),
            episodeDetailsUpdates.observeUpdates(Source.SEASON),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }
                val authenticated = sessionManager.isAuthenticated()

                val watched = when {
                    !authenticated -> null
                    else -> when {
                        loadUserProgressUseCase.isShowsLoaded() -> {
                            loadUserProgressUseCase.loadLocalShows()
                        }
                        else -> loadUserProgressUseCase.loadShowsProgress()
                    }.firstOrNull {
                        it.show.ids.trakt == show.ids.trakt
                    }
                }

                val seasons = getSeasonsUseCase.getAllSeasons(
                    showId = show.ids.trakt,
                    initialSeason = watched?.seasons
                        ?.maxByOrNull { it.number }
                        ?.number
                        ?: 1,
                )

                itemsState.update {
                    seasons.copy(
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = seasons.selectedSeasonEpisodes,
                            progress = watched?.seasons,
                            checkable = authenticated,
                        ),
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    fun loadSeason(season: Season) {
        if (
            loadingEpisodeState.value.isLoading ||
            itemsState.value.isSeasonLoading ||
            season.number == itemsState.value.selectedSeason?.number
        ) {
            return
        }

        viewModelScope.launch {
            try {
                itemsState.update {
                    it.copy(isSeasonLoading = true)
                }

                val authenticated = sessionManager.isAuthenticated()

                itemsState.update {
                    it.copy(selectedSeason = season)
                }

                val progress = when {
                    authenticated -> when {
                        loadUserProgressUseCase.isShowsLoaded() -> {
                            loadUserProgressUseCase.loadLocalShows()
                        }
                        else -> loadUserProgressUseCase.loadShowsProgress()
                    }.firstOrNull {
                        it.show.ids.trakt == show.ids.trakt
                    }
                    else -> null
                }

                val episodes = getSeasonsUseCase.getSeasonEpisodes(
                    showId = show.ids.trakt,
                    season = season.number,
                )

                itemsState.update {
                    it.copy(
                        selectedSeason = season,
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = episodes,
                            progress = progress?.seasons,
                            checkable = authenticated,
                        ),
                        isSeasonLoading = false,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w("Error loading season: ${error.message}")
                    itemsState.update { it.copy(isSeasonLoading = false) }
                }
            }
        }
    }

    fun addToWatched(episode: Episode) {
        if (loadingState.value.isLoading || loadingEpisodeState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            val authenticated = sessionManager.isAuthenticated()
            if (!authenticated) {
                return@launch
            }

            try {
                loadingEpisodeState.update { LOADING }
                setLoadingEpisode(episode)

                updateEpisodeHistoryUseCase.addToHistory(episode.ids.trakt)
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == show.ids.trakt
                    }

                itemsState.update {
                    it.copy(
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(SEASONS)

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }
                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "show_seasons_view",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingEpisodeState.update { DONE }
            }
        }
    }

    fun removeFromWatched(episode: Episode) {
        if (loadingState.value.isLoading || loadingEpisodeState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            val authenticated = sessionManager.isAuthenticated()
            if (!authenticated) {
                return@launch
            }

            try {
                loadingEpisodeState.update { LOADING }
                setLoadingEpisode(episode)

                updateEpisodeHistoryUseCase.removeEpisodeFromHistory(episode.ids.trakt.value)
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == show.ids.trakt
                    }

                itemsState.update {
                    it.copy(
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(SEASONS)
                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingEpisodeState.update { DONE }
            }
        }
    }

    private suspend fun setLoadingEpisode(episode: Episode) {
        itemsState.update {
            it.copy(
                selectedSeasonEpisodes = it.selectedSeasonEpisodes
                    .asyncMap { e ->
                        e.copy(
                            isLoading = (episode.ids.trakt == e.episode.ids.trakt),
                        )
                    }.toImmutableList(),
            )
        }
    }

    private suspend fun markWatchedEpisodes(
        inputEpisodes: List<EpisodeItem>,
        progress: ImmutableList<ProgressItem.ShowItem.Season>?,
        checkable: Boolean,
    ): ImmutableList<EpisodeItem> {
        return inputEpisodes
            .asyncMap {
                it.copy(
                    isLoading = false,
                    isCheckable = checkable,
                    isWatched = progress
                        ?.firstOrNull { s -> s.number == it.episode.season }
                        ?.episodes
                        ?.any { e -> e.number == it.episode.number } == true,
                )
            }.toImmutableList()
    }

    fun clearInfo() {
        infoState.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowSeasonsState> = combine(
        showState,
        itemsState,
        loadingState,
        loadingEpisodeState,
        infoState,
        errorState,
    ) { state ->
        ShowSeasonsState(
            show = state[0] as Show,
            items = state[1] as ShowSeasons,
            loading = state[2] as LoadingState,
            loadingEpisode = state[3] as LoadingState,
            info = state[4] as StringResource?,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
