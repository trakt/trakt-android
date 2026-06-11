package tv.trakt.trakt.core.share

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SlugId

@Immutable
internal data class ShareState(
    val media: Media? = null,
    val loading: LoadingState = LoadingState.Idle,
) {
    data class Media(
        val type: MediaType,
        val slug: SlugId,
    )
}
