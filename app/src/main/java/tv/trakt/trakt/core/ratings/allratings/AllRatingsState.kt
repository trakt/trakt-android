package tv.trakt.trakt.core.ratings.allratings

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Season

@Immutable
internal data class AllRatingsState(
    val seasons: ImmutableList<Season>? = null,
    val loading: LoadingState = LoadingState.Idle,
)
