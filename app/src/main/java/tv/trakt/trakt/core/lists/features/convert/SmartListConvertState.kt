package tv.trakt.trakt.core.lists.features.convert

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListDraft
import tv.trakt.trakt.core.lists.model.SmartListItem

@Immutable
internal data class SmartListConvertState(
    val list: CustomList? = null,
    val draft: SmartListDraft? = null,
    val currentItems: ImmutableList<SmartListItem>? = null,
    val proposedItems: ImmutableList<SmartListItem>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val converting: LoadingState = LoadingState.Idle,
    val missing: Boolean = false,
    val convertedList: SmartList? = null,
    val error: Exception? = null,
)
