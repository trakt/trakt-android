package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.WatchedUntil
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.helpers.computeWatchedTimestamps
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.usecases.GetWatchedUntilEpisodesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

internal class WatchedUntilViewModel(
    private val show: Show,
    private val episode: Episode,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val getWatchedUntilUseCase: GetWatchedUntilEpisodesUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val showDetailsUpdates: ShowDetailsUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = WatchedUntilState(show = show)

    private val episodesState = MutableStateFlow(initialState.episodes)
    private val successState = MutableStateFlow(initialState.success)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingWatchedState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        if (loadingState.value.isLoading || loadingWatchedState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                delay(300.milliseconds) // Delay to let the sheet show in fully.

                episodesState.update {
                    getWatchedUntilUseCase.getEpisodes(
                        show = show,
                        selectedEpisode = episode,
                    )
                }

                loadingState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
                loadingState.update { Idle }
            }
        }
    }

    fun addToWatched(
        action: WatchedUntilAction,
        otherBound: OtherDateBound?,
        otherAnchor: Instant?,
    ) {
        if (loadingState.value.isLoading || loadingWatchedState.value.isLoading) {
            return
        }

        val episodes = episodesState.value ?: return
        val timestamps = computeWatchedTimestamps(
            episodes = episodes,
            action = action,
            otherBound = otherBound,
            otherAnchor = otherAnchor,
        )
        val entries = episodes.mapIndexedNotNull { index, episode ->
            timestamps.getOrNull(index)?.let { watchedAt ->
                episode.ids.trakt to watchedAt
            }
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated() || entries.isEmpty()) {
                return@launch
            }

            try {
                loadingState.update { Loading }
                loadingWatchedState.update { Loading }

                updateEpisodeHistoryUseCase.addToHistory(
                    episodes = entries.map { (episodeId, watchedAt) ->
                        episodeId to watchedAt.toString()
                    },
                )
                loadUserProgressUseCase.loadShowsProgress()

                showDetailsUpdates.notifyUpdate(WatchedUntil)
                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "watched_until",
                    date = null,
                )

                successState.update { true }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
                loadingState.update { Done }
                loadingWatchedState.update { Done }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        episodesState,
        successState,
        loadingState,
        loadingWatchedState,
        errorState,
    ) { state ->
        WatchedUntilState(
            show = show,
            episodes = state[0] as ImmutableList<Episode>?,
            success = state[1] as Boolean?,
            loading = state[2] as LoadingState,
            loadingWatched = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
