package tv.trakt.trakt.core.today.stories.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.core.today.usecases.GetTodayDetailsUseCase

@Suppress("UNCHECKED_CAST")
internal class TodayPosterDetailsViewModel(
    private val media: TodayMedia,
    private val getTodayDetailsUseCase: GetTodayDetailsUseCase,
) : ViewModel() {
    private val initialState = TodayPosterDetailsState()

    private val ratingsState = MutableStateFlow(initialState.ratings)
    private val sentimentState = MutableStateFlow(initialState.sentiment)
    private val loadingState = MutableStateFlow(initialState.loading)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            loadingState.update { Loading }

            listOf(
                launch { loadRatings() },
                launch { loadSentiment() },
            ).joinAll()

            loadingState.update { Done }
        }
    }

    private suspend fun loadRatings() {
        try {
            ratingsState.update { getTodayDetailsUseCase.getRatings(media) }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    private suspend fun loadSentiment() {
        try {
            sentimentState.update { getTodayDetailsUseCase.getSentiment(media) }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    val state = combine(
        ratingsState,
        sentimentState,
        loadingState,
    ) { state ->
        TodayPosterDetailsState(
            ratings = state[0] as ExternalRating?,
            sentiment = state[1] as Sentiments?,
            loading = state[2] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
