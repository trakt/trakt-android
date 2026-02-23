package tv.trakt.trakt.app.core.details.lists.details.media

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem

@Immutable
internal data class CustomListMediaState(
    val items: ImmutableList<ListMediaItem>? = null,
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val error: Exception? = null,
)
