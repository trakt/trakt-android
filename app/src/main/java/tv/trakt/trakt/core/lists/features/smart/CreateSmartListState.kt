package tv.trakt.trakt.core.lists.features.smart

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.core.lists.model.SmartListItem

@Immutable
internal data class CreateSmartListState(
    val items: ImmutableList<SmartListItem>? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val filters: SmartListFilters = SmartListFilters.Default,
    val creating: LoadingState = LoadingState.Idle,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
    val success: Boolean = false,
)
