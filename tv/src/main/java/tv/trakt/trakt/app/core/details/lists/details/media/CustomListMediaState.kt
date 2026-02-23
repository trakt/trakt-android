package tv.trakt.trakt.app.core.details.lists.details.media

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.common.helpers.StringResource

@Immutable
internal data class CustomListMediaState(
    val items: ImmutableList<ListMediaItem>? = null,
    val like: LikedState = LikedState(),
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val info: StringResource? = null,
    val error: Exception? = null,
) {
    data class LikedState(
        val likesCount: Int = 0,
        val isLiked: Boolean = false,
        val isLoading: Boolean = false,
    )
}
