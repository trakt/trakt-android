package tv.trakt.trakt.core.today.stories.details

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Sentiments

@Immutable
internal data class TodayPosterDetailsState(
    val ratings: ExternalRating? = null,
    val sentiment: Sentiments? = null,
    val loading: LoadingState = LoadingState.Idle,
)
