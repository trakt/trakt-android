package tv.trakt.trakt.core.summary.social.recommendedby

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.summary.social.recommendedby.model.RecommendedBy

@Immutable
internal data class RecommendedByState(
    val recommendedBy: RecommendedBy? = null,
    val loading: LoadingState = LoadingState.Idle,
)
