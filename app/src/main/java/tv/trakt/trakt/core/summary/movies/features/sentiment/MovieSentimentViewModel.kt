package tv.trakt.trakt.core.summary.movies.features.sentiment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.core.summary.movies.features.sentiment.usecases.GetMovieSentimentUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class MovieSentimentViewModel(
    private val movie: Movie,
    private val getSentimentUseCase: GetMovieSentimentUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = MovieSentimentState()

    private val sentimentState = MutableStateFlow(initialState.sentiment)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.MOVIE_SENTIMENT))
    private val errorState = MutableStateFlow(initialState.error)

    private var collapseJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                sentimentState.update {
                    getSentimentUseCase.getSentiments(movie.ids.trakt)
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
                collapsed -> collapsingManager.collapse(CollapsingKey.MOVIE_SENTIMENT)
                else -> collapsingManager.expand(CollapsingKey.MOVIE_SENTIMENT)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<MovieSentimentState> = combine(
        sentimentState,
        loadingState,
        collapseState,
        errorState,
    ) { state ->
        MovieSentimentState(
            sentiment = state[0] as Sentiments?,
            loading = state[1] as LoadingState,
            collapsed = state[2] as Boolean,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
