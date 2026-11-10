package tv.trakt.trakt.core.summary.shows.features.actors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.user.usecases.spoilers.GetSpoilerStateUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.features.actors.usecases.GetShowActorsUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class ShowActorsViewModel(
    private val show: Show,
    private val getActorsUseCase: GetShowActorsUseCase,
    private val getSpoilerStateUseCase: GetSpoilerStateUseCase,
    private val analytics: Analytics,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = ShowActorsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.SHOW_ACTORS))
    private val spoilersHiddenState = MutableStateFlow(initialState.spoilersHidden)

    private var collapseJob: Job? = null

    init {
        loadData()
        observeSpoilers()
    }

    private fun observeSpoilers() {
        getSpoilerStateUseCase.observeShowHidden(show.ids.trakt)
            .onEach { hidden -> spoilersHiddenState.update { hidden } }
            .catch { error -> Timber.recordError(error) }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val spoilersHidden = async { loadSpoilersHidden() }
                val items = getActorsUseCase.getCastCrew(show.ids.trakt)

                spoilersHiddenState.update { spoilersHidden.await() }
                itemsState.update { items }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadSpoilersHidden(): Boolean {
        return try {
            getSpoilerStateUseCase.isShowHidden(show.ids.trakt)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            // Unknown watched progress must not expose episode counts.
            true
        }
    }

    fun logEpisodeCountReveal() {
        analytics.logSpoilerReveal(MediaType.Show.value)
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }
        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.SHOW_ACTORS)
                else -> collapsingManager.expand(CollapsingKey.SHOW_ACTORS)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowActorsState> = combine(
        itemsState,
        loadingState,
        errorState,
        collapseState,
        spoilersHiddenState,
    ) { state ->
        ShowActorsState(
            items = state[0] as ImmutableList<CastPerson>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
            collapsed = state[3] as Boolean,
            spoilersHidden = state[4] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
