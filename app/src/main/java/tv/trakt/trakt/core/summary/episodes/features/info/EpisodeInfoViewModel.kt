@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.summary.episodes.features.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.networking.EpisodeStatsDto
import tv.trakt.trakt.core.summary.episodes.features.info.usecase.GetEpisodeCrewUseCase
import tv.trakt.trakt.core.summary.episodes.features.info.usecase.GetEpisodeStatsUseCase

internal class EpisodeInfoViewModel(
    private val show: Show,
    private val episode: Episode,
    private val getStatsUseCase: GetEpisodeStatsUseCase,
    private val getCrewUseCase: GetEpisodeCrewUseCase,
) : ViewModel() {
    private val initialState = EpisodeInfoState()

    private val showState = MutableStateFlow(show)
    private val episodeState = MutableStateFlow(episode)
    private val episodeStatsState = MutableStateFlow(initialState.episodeStats)
    private val episodeCrewState = MutableStateFlow(initialState.episodeCrew)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                coroutineScope {
                    awaitAll(
                        async { loadStats() },
                        async { loadCrew() },
                    )
                }
                loadingState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            }
        }
    }

    private suspend fun loadStats() {
        try {
            episodeStatsState.update {
                getStatsUseCase.getStats(
                    showId = show.ids.trakt,
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

    private suspend fun loadCrew() {
        try {
            episodeCrewState.update {
                getCrewUseCase.getCrew(
                    showId = show.ids.trakt,
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

    val state = combine(
        showState,
        episodeState,
        episodeStatsState,
        episodeCrewState,
        loadingState,
        errorState,
    ) { state ->
        EpisodeInfoState(
            show = state[0] as Show?,
            episode = state[1] as Episode?,
            episodeStats = state[2] as EpisodeStatsDto?,
            episodeCrew = state[3] as GetEpisodeCrewUseCase.Result?,
            loading = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
