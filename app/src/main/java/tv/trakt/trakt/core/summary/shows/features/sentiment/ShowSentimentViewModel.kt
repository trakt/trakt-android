package tv.trakt.trakt.core.summary.shows.features.sentiment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.features.sentiment.usecases.GetShowSentimentUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class ShowSentimentViewModel(
    private val show: Show,
    private val getSentimentUseCase: GetShowSentimentUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = ShowSentimentState()

    private val sentimentState = MutableStateFlow(initialState.sentiment)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.SHOW_SENTIMENT))

    private var collapseJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                sentimentState.update {
                    getSentimentUseCase.getSentiments(show.ids.trakt)
                }
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
                collapsed -> collapsingManager.collapse(CollapsingKey.SHOW_SENTIMENT)
                else -> collapsingManager.expand(CollapsingKey.SHOW_SENTIMENT)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowSentimentState> = combine(
        sentimentState,
        loadingState,
        errorState,
        collapseState,
    ) { state ->
        ShowSentimentState(
            sentiment = state[0] as Sentiments?,
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
