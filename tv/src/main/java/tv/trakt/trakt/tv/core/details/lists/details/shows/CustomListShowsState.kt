package tv.trakt.trakt.tv.core.details.lists.details.shows

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.tv.core.shows.model.Show

@Immutable
internal data class CustomListShowsState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val shows: ImmutableList<Show>? = null,
    val error: Exception? = null,
)
