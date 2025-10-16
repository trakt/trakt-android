package tv.trakt.trakt.core.summary.episodes.features.comments

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
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.episodes.features.comments.usecases.GetEpisodeCommentsUseCase

internal class EpisodeCommentsViewModel(
    private val show: Show,
    private val episode: Episode,
    private val getCommentsUseCase: GetEpisodeCommentsUseCase,
) : ViewModel() {
    private val initialState = EpisodeCommentsState()

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

                val items = getCommentsUseCase.getComments(
                    showId = show.ids.trakt,
                    seasonEpisode = episode.seasonEpisode,
                )

                itemsState.update { items }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<EpisodeCommentsState> = combine(
        itemsState,
        loadingState,
        errorState,
    ) { state ->
        EpisodeCommentsState(
            items = state[0] as ImmutableList<Comment>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
