package tv.trakt.trakt.core.summary.movies.features.sentiment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Sentiments

@Immutable
internal data class MovieSentimentState(
    val sentiment: Sentiments? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)
