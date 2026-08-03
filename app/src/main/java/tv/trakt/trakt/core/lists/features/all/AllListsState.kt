package tv.trakt.trakt.core.lists.features.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.ListsItem
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType

@Immutable
internal data class AllListsState(
    val user: User? = null,
    val items: ImmutableList<ListsItem>? = null,
    val filter: PersonalListType? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val info: DynamicStringResource? = null,
    val error: Exception? = null,
)
