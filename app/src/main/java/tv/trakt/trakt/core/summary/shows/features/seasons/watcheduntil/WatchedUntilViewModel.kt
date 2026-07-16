package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.usecases.GetWatchedUntilEpisodesUseCase
import kotlin.time.Duration.Companion.milliseconds

internal class WatchedUntilViewModel(
    private val show: Show,
    private val episode: Episode,
    private val getWatchedUntilUseCase: GetWatchedUntilEpisodesUseCase,
) : ViewModel() {
    private val initialState = WatchedUntilState(show = show)

    private val episodesState = MutableStateFlow(initialState.episodes)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        if (loadingState.value.isLoading) {
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

    val state = combine(
        episodesState,
        loadingState,
        errorState,
    ) { s1, s2, s3 ->
        WatchedUntilState(
            show = show,
            episodes = s1,
            loading = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
