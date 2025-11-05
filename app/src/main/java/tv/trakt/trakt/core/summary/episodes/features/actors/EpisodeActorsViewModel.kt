package tv.trakt.trakt.core.summary.episodes.features.actors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.episodes.features.actors.usecases.GetEpisodeActorsUseCase

internal class EpisodeActorsViewModel(
    private val show: Show,
    private val episode: Episode,
    private val getActorsUseCase: GetEpisodeActorsUseCase,
) : ViewModel() {
    private val initialState = EpisodeActorsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                itemsState.update {
                    getActorsUseCase.getCastCrew(
                        showId = show.ids.trakt,
                        season = episode.season,
                        episode = episode.number,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<EpisodeActorsState> = combine(
        itemsState,
        loadingState,
        errorState,
    ) { state ->
        EpisodeActorsState(
            items = state[0] as ImmutableList<CastPerson>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
