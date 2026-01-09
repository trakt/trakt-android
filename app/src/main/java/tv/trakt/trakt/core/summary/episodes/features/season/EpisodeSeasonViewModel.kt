package tv.trakt.trakt.core.summary.episodes.features.season

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source
import tv.trakt.trakt.core.summary.episodes.features.season.usecases.GetEpisodeSeasonUseCase
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.SEASONS
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.core.sync.model.ProgressItem
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

@OptIn(FlowPreview::class)
internal class EpisodeSeasonViewModel(
    private val show: Show,
    private val episode: Episode,
    private val getSeasonDetailsUseCase: GetEpisodeSeasonUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val showDetailsUpdates: ShowDetailsUpdates,
    private val episodeDetailsUpdates: EpisodeDetailsUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = EpisodeSeasonState()

    private val showState = MutableStateFlow(show)
    private val episodeState = MutableStateFlow(episode)
    private val episodesState = MutableStateFlow(initialState.episodes)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingEpisodeState = MutableStateFlow(initialState.loadingEpisode)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.EPISODE_SEASON))

    private var collapseJob: Job? = null

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        merge(
            showDetailsUpdates.observeUpdates(PROGRESS),
            showDetailsUpdates.observeUpdates(SEASONS),
            episodeDetailsUpdates.observeUpdates(Source.PROGRESS),
        )
            .distinctUntilChanged()
            .debounce(200L)
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

                val episodesAsync = async {
                    getSeasonDetailsUseCase.getSeasonEpisodes(
                        showId = show.ids.trakt,
                        seasonNumber = episode.season,
                    )
                }
                val watchedAsync = async {
                    if (!authenticated) {
                        return@async null
                    }
                    when {
                        loadUserProgressUseCase.isShowsLoaded() -> {
                            loadUserProgressUseCase.loadLocalShows()
                        }

                        else -> {
                            loadUserProgressUseCase.loadShowsProgress()
                        }
                    }.firstOrNull {
                        it.show.ids.trakt == show.ids.trakt
                    }
                }

                val episodes = episodesAsync.await()
                val watched = watchedAsync.await()

                val markedEpisodes = markWatchedEpisodes(
                    inputEpisodes = episodes,
                    progress = watched?.seasons
                        ?.firstOrNull { it.number == episode.season }
                        ?.episodes,
                    checkable = authenticated,
                )

                episodesState.update { markedEpisodes.toImmutableList() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    fun addToWatched(
        episodeToAdd: Episode,
        customDate: DateSelectionResult? = null,
    ) {
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
                setLoadingEpisode(episodeToAdd)

                updateEpisodeHistoryUseCase.addToHistory(
                    episodeId = episodeToAdd.ids.trakt,
                    customDate = customDate,
                )
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == show.ids.trakt
                    }

                episodesState.update {
                    markWatchedEpisodes(
                        inputEpisodes = episodesState.value,
                        progress = progress?.seasons
                            ?.firstOrNull { s -> s.number == episode.season }
                            ?.episodes,
                        checkable = true,
                    )
                }

                episodeDetailsUpdates.notifyUpdate(Source.SEASON)
                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }
                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "episode_season_view",
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingEpisodeState.update { DONE }
            }
        }
    }

    fun removeFromWatched(episodeToRemove: Episode) {
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
                setLoadingEpisode(episodeToRemove)

                updateEpisodeHistoryUseCase.removeEpisodeFromHistory(episodeToRemove.ids.trakt.value)
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == show.ids.trakt
                    }

                episodesState.update {
                    markWatchedEpisodes(
                        inputEpisodes = episodesState.value,
                        progress = progress?.seasons
                            ?.firstOrNull { s -> s.number == episode.season }
                            ?.episodes,
                        checkable = true,
                    )
                }

                episodeDetailsUpdates.notifyUpdate(Source.SEASON)
                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "episode",
                    source = "episode_season_view",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingEpisodeState.update { DONE }
            }
        }
    }

    private suspend fun setLoadingEpisode(episodeToSet: Episode) {
        episodesState.update {
            it.asyncMap { e ->
                e.copy(
                    isLoading = (episodeToSet.ids.trakt == e.episode.ids.trakt),
                )
            }.toImmutableList()
        }
    }

    private suspend fun markWatchedEpisodes(
        inputEpisodes: List<EpisodeItem>,
        progress: ImmutableList<ProgressItem.ShowItem.Episode>?,
        checkable: Boolean,
    ): ImmutableList<EpisodeItem> {
        return inputEpisodes
            .asyncMap { item ->
                item.copy(
                    isLoading = false,
                    isCheckable = checkable,
                    isWatched = progress
                        ?.any { episodeProgress -> episodeProgress.number == item.episode.number } == true,
                )
            }.toImmutableList()
    }

    fun clearInfo() {
        infoState.update { null }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }
        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.EPISODE_SEASON)
                else -> collapsingManager.expand(CollapsingKey.EPISODE_SEASON)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<EpisodeSeasonState> = combine(
        episodeState,
        showState,
        episodesState,
        loadingState,
        loadingEpisodeState,
        infoState,
        errorState,
        collapseState,
    ) { state ->
        EpisodeSeasonState(
            episode = state[0] as Episode,
            show = state[1] as Show,
            episodes = state[2] as ImmutableList<EpisodeItem>,
            loading = state[3] as LoadingState,
            loadingEpisode = state[4] as LoadingState,
            info = state[5] as StringResource?,
            error = state[6] as Exception?,
            collapsed = state[7] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
