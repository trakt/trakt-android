package tv.trakt.trakt.core.summary.shows.features.sentiment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ShowSentimentState(
    val user: User? = null,
    val sentiment: Sentiments? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
    val collapsed: Boolean? = null,
)
