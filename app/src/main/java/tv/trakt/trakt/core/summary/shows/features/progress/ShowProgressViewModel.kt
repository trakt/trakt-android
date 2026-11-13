package tv.trakt.trakt.core.summary.shows.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.sync.model.ProgressItem.ShowItem
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.shows.features.progress.ShowProgressState.UpNext
import tv.trakt.trakt.core.summary.shows.features.progress.model.SeasonProgress
import tv.trakt.trakt.core.summary.shows.features.progress.usecases.GetShowProgressUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import kotlin.time.Duration.Companion.milliseconds

private const val DRILLDOWN_SOURCE = "watch-state"
private const val HISTORY_SOURCE = "show_progress"

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class ShowProgressViewModel(
    private val show: Show,
    private val getProgressUseCase: GetShowProgressUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ShowProgressState(show = show)
    private val origin = ChangeOrigin.unique()

    private val showState = MutableStateFlow(initialState.show)
    private val seasonsState = MutableStateFlow(initialState.seasons)
    private val episodesState = MutableStateFlow(initialState.episodes)
    private val expandedState = MutableStateFlow(initialState.expanded)
    private val upNextState = MutableStateFlow(initialState.upNext)
    private val watchedEpisodesState = MutableStateFlow(initialState.watchedEpisodes)
    private val busyEpisodesState = MutableStateFlow(initialState.busyEpisodes)
    private val loadingSeasonsState = MutableStateFlow(initialState.loadingSeasons)
    private val errorState = MutableStateFlow(initialState.error)

    private var seasons: List<Season> = emptyList()
    private var loadJob: Job? = null
    private val historyMutex = Mutex()

    init {
        loadData()
        observeData()

        analytics.logSummaryDrilldown(source = DRILLDOWN_SOURCE, type = "show")
    }

    private fun observeData() {
        dataChanges.observe(Kind.History, exclude = origin)
            .debounce(200.milliseconds)
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                coroutineScope {
                    val seasonsAsync = async {
                        seasons.ifEmpty { getProgressUseCase.getSeasons(show.ids.trakt) }
                    }
                    val progressAsync = async {
                        getProgressUseCase.getUserProgress(show.ids.trakt, refresh = false)
                    }
                    val upNextAsync = async { loadUpNext() }

                    seasons = seasonsAsync.await()
                    val progress = progressAsync.await()
                    val upNext = upNextAsync.await()
                    val seasonProgress = SeasonProgress.fromSeasons(seasons, progress)

                    resolveCurrentSeason(seasonProgress, upNext)
                        ?.takeIf { expandedState.value[it] != false }
                        ?.let { preloadEpisodes(it) }

                    upNextState.update { upNext }
                    applyProgress(progress, seasonProgress)
                }
                loadExpandedEpisodes()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    private suspend fun loadUpNext(): UpNext? {
        return try {
            getProgressUseCase.getUpNext(show.ids.trakt)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            null
        }
    }

    private fun applyProgress(
        progress: ShowItem?,
        seasonProgress: ImmutableList<SeasonProgress> = SeasonProgress.fromSeasons(seasons, progress),
    ) {
        watchedEpisodesState.update { progress?.watchedEpisodesWithoutSpecials ?: 0 }
        seasonsState.update { seasonProgress }
    }

    private suspend fun preloadEpisodes(season: Int) {
        if (episodesState.value.containsKey(season)) {
            return
        }

        try {
            val episodes = getProgressUseCase.getSeasonEpisodes(show.ids.trakt, season)
            episodesState.update { it.toPersistentMap().put(season, episodes) }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    private fun isExpanded(season: Int): Boolean {
        val current = resolveCurrentSeason(seasonsState.value, upNextState.value)
        return expandedState.value[season] ?: (season == current)
    }

    private fun loadExpandedEpisodes() {
        seasonsState.value
            ?.filter { isExpanded(it.number) }
            ?.forEach { loadEpisodes(it.number) }
    }

    private fun loadEpisodes(
        season: Int,
        onLoaded: () -> Unit = {},
    ) {
        if (episodesState.value.containsKey(season) || season in loadingSeasonsState.value) {
            return
        }
        loadingSeasonsState.update { it.toPersistentSet().add(season) }

        viewModelScope.launch {
            try {
                val episodes = getProgressUseCase.getSeasonEpisodes(show.ids.trakt, season)
                episodesState.update { it.toPersistentMap().put(season, episodes) }
                onLoaded()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingSeasonsState.update { it.toPersistentSet().remove(season) }
            }
        }
    }

    fun toggleSeason(season: Int) {
        if (season in loadingSeasonsState.value) {
            return
        }

        val loaded = episodesState.value.containsKey(season)
        if (loaded && isExpanded(season)) {
            setExpanded(season, false)
            return
        }

        analytics.logSummaryDrilldown(source = DRILLDOWN_SOURCE, type = "season")
        when {
            loaded -> setExpanded(season, true)
            else -> loadEpisodes(season) { setExpanded(season, true) }
        }
    }

    private fun setExpanded(
        season: Int,
        expanded: Boolean,
    ) {
        expandedState.update { it.toPersistentMap().put(season, expanded) }
    }

    fun addToWatched(
        episode: Episode,
        customDate: DateSelectionResult?,
    ) {
        addEpisodes(
            episodes = listOf(episode),
            mediaType = "episode",
            customDate = customDate,
        )
    }

    fun finishSeason(
        episodes: List<Episode>,
        customDate: DateSelectionResult?,
    ) {
        addEpisodes(
            episodes = episodes,
            mediaType = "season",
            customDate = customDate,
        )
    }

    private fun addEpisodes(
        episodes: List<Episode>,
        mediaType: String,
        customDate: DateSelectionResult?,
    ) {
        val ids = episodes.map { it.ids.trakt }
        updateHistory(ids) {
            updateEpisodeHistoryUseCase.addToHistory(
                episodeIds = ids,
                customDate = customDate,
                origin = origin,
            )
            analytics.progress.logAddWatchedMedia(
                mediaType = mediaType,
                source = HISTORY_SOURCE,
                date = customDate?.analyticsStrings,
            )
        }
    }

    fun removeFromWatched(episode: Episode) {
        updateHistory(listOf(episode.ids.trakt)) {
            updateEpisodeHistoryUseCase.removeEpisodeFromHistory(
                episodeId = episode.ids.trakt.value,
                origin = origin,
            )
            analytics.progress.logRemoveWatchedMedia(
                mediaType = "episode",
                source = HISTORY_SOURCE,
            )
        }
    }

    private fun updateHistory(
        ids: List<TraktId>,
        write: suspend () -> Unit,
    ) {
        if (ids.isEmpty() || ids.any { it in busyEpisodesState.value }) {
            return
        }

        busyEpisodesState.update { (it + ids).toImmutableSet() }

        viewModelScope.launch {
            try {
                historyMutex.withLock {
                    write()
                    refreshProgress()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                busyEpisodesState.update { (it - ids.toSet()).toImmutableSet() }
            }
        }
    }

    private suspend fun refreshProgress() {
        try {
            applyProgress(getProgressUseCase.getUserProgress(show.ids.trakt, refresh = false))
            upNextState.update { loadUpNext() }
            loadExpandedEpisodes()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    val state = combine(
        showState,
        seasonsState,
        episodesState,
        expandedState,
        upNextState,
        watchedEpisodesState,
        busyEpisodesState,
        loadingSeasonsState,
        errorState,
    ) { state ->
        ShowProgressState(
            show = state[0] as Show?,
            seasons = state[1] as ImmutableList<SeasonProgress>?,
            episodes = state[2] as ImmutableMap<Int, ImmutableList<Episode>>,
            expanded = state[3] as ImmutableMap<Int, Boolean>,
            upNext = state[4] as UpNext?,
            watchedEpisodes = state[5] as Int,
            busyEpisodes = state[6] as ImmutableSet<TraktId>,
            loadingSeasons = state[7] as ImmutableSet<Int>,
            error = state[8] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
