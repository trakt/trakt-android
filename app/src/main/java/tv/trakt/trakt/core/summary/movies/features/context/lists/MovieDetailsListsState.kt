package tv.trakt.trakt.core.summary.movies.features.context.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.CustomListMinimal
import tv.trakt.trakt.common.model.User

private val EmptyList = emptyList<Pair<CustomListMinimal, Boolean>>().toImmutableList()

@Immutable
internal data class MovieDetailsListsState(
    val lists: ImmutableList<Pair<CustomListMinimal, Boolean>> = EmptyList,
    val user: User? = null,
    val error: Exception? = null,
)
