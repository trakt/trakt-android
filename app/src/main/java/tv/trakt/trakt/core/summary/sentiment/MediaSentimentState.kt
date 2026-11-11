package tv.trakt.trakt.core.summary.sentiment

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.realitycheck.RealityCheck

@Immutable
internal data class MediaSentimentState(
    val user: User? = null,
    val sentiment: Sentiments? = null,
    val realityCheck: RealityCheck? = null,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
) {
    val hasInsights: Boolean
        get() = sentiment?.isEmpty == false || realityCheck != null
}
