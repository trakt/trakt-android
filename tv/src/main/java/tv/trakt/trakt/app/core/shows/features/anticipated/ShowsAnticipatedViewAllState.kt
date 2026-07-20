package tv.trakt.trakt.app.core.shows.features.anticipated

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.shows.model.AnticipatedShow
import tv.trakt.trakt.common.core.user.UserCollectionState

@Immutable
internal data class ShowsAnticipatedViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val shows: ImmutableList<AnticipatedShow>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)
