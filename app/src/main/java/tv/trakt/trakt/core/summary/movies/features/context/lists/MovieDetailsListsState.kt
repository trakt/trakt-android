package tv.trakt.trakt.core.summary.movies.features.context.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.model.CustomListMinimal
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User

@Immutable
internal data class MovieDetailsListsState(
    val lists: ImmutableList<CustomListMinimal> = EmptyImmutableList,
    val movieLists: ImmutableSet<TraktId> = EmptyImmutableSet,
    val user: User? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
