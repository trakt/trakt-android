package tv.trakt.trakt.core.shows.sections.popular

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class ShowsPopularState(
    val items: ImmutableList<Show>? = null,
    val loading: Boolean = true,
    val error: Exception? = null,
)
