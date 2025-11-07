package tv.trakt.trakt.core.discover.sections.popular.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class AllShowsPopularState(
    val items: ImmutableList<Show>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingMore: LoadingState = LoadingState.IDLE,
    val backgroundUrl: String? = null,
    val error: Exception? = null,
)
