package tv.trakt.trakt.core.summary.episodes.features.streaming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.streamings.model.StreamingsResult
import tv.trakt.trakt.core.summary.episodes.features.streaming.usecases.GetEpisodeStreamingsUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class EpisodeStreamingsViewModel(
    private val show: Show,
    private val episode: Episode,
    private val sessionManager: SessionManager,
    private val getStreamingsUseCase: GetEpisodeStreamingsUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = EpisodeStreamingsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.EPISODE_WHERE_TO_WATCH))

    private var collapseJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                val user = sessionManager.getProfile()
                if (user == null) {
                    itemsState.update { null }
                    loadingState.update { DONE }
                    return@launch
                }

                val items = getStreamingsUseCase.getStreamings(
                    user = user,
                    show = show,
                    episode = episode,
                )

                itemsState.update { items }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }
        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.EPISODE_WHERE_TO_WATCH)
                else -> collapsingManager.expand(CollapsingKey.EPISODE_WHERE_TO_WATCH)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        itemsState,
        loadingState,
        errorState,
        collapseState,
    ) { state ->
        EpisodeStreamingsState(
            items = state[0] as StreamingsResult?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
            collapsed = state[3] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
