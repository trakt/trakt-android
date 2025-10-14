package tv.trakt.trakt.core.summary.shows.features.actors

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.CastPerson

@Immutable
internal data class ShowActorsState(
    val items: ImmutableList<CastPerson>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
