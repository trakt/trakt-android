package tv.trakt.trakt.common.core.lists.manage

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomListMinimal

@Immutable
data class ManageListsState(
    val lists: ImmutableList<CustomListMinimal> = EmptyImmutableList,
    val collaborations: ImmutableList<CustomListMinimal> = EmptyImmutableList,
    val listedIds: ImmutableSet<TraktId> = EmptyImmutableSet,
    val toggling: ImmutableSet<TraktId> = EmptyImmutableSet,
    val opening: TraktId? = null,
    val user: User? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
