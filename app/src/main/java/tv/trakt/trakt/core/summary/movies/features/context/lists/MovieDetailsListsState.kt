package tv.trakt.trakt.core.summary.movies.features.context.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.User

private val EmptyList = emptyList<Pair<CustomList, Boolean>>().toImmutableList()

@Immutable
internal data class MovieDetailsListsState(
    val lists: ImmutableList<Pair<CustomList, Boolean>> = EmptyList,
    val user: User? = null,
    val error: Exception? = null,
)
