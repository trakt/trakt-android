package tv.trakt.trakt.core.summary.shows.features.sentiment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Sentiments

@Immutable
internal data class ShowSentimentState(
    val sentiment: Sentiments? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
