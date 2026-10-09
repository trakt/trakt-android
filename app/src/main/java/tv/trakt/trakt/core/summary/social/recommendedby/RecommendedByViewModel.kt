package tv.trakt.trakt.core.summary.social.recommendedby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.applinks.ShareArrivalEvents
import tv.trakt.trakt.core.summary.social.recommendedby.model.RecommendedBy
import tv.trakt.trakt.core.summary.social.recommendedby.usecases.GetRecommendedByUseCase

/**
 * @param path Relative web path of the item, e.g. `/movies/<slug>`.
 */
@Suppress("UNCHECKED_CAST")
internal class RecommendedByViewModel(
    private val path: String,
    private val getRecommendedByUseCase: GetRecommendedByUseCase,
    private val shareArrivalEvents: ShareArrivalEvents,
) : ViewModel() {
    private val initialState = RecommendedByState()

    private val recommendedByState = MutableStateFlow(initialState.recommendedBy)
    private val loadingState = MutableStateFlow(initialState.loading)

    private var loadJob: Job? = null

    init {
        loadData()
        observeShareArrivals()
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }
                recommendedByState.update {
                    getRecommendedByUseCase.getRecommendedBy(path)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Failed to load recommended by")
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private fun observeShareArrivals() {
        viewModelScope.launch {
            shareArrivalEvents.credited
                .drop(1)
                .collect { loadData() }
        }
    }

    val state = combine(
        recommendedByState,
        loadingState,
    ) { state ->
        RecommendedByState(
            recommendedBy = state[0] as RecommendedBy?,
            loading = state[1] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
