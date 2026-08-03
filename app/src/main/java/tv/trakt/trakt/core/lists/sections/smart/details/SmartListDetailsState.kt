package tv.trakt.trakt.core.lists.sections.smart.details

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.core.lists.model.SmartListItem

@Immutable
internal data class SmartListDetailsState(
    val list: SmartList? = null,
    val items: ImmutableList<SmartListItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val deleting: LoadingState = LoadingState.Idle,
    val deleted: Boolean = false,
    val error: Exception? = null,
)
