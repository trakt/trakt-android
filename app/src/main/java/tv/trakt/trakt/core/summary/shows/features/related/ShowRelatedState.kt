package tv.trakt.trakt.core.summary.shows.features.related

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class ShowRelatedState(
    val items: ImmutableList<Show>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
    val collapsed: Boolean? = null,
)
