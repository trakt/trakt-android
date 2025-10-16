package tv.trakt.trakt.core.summary.episodes.features.context.more

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.streamings.StreamingService

@Immutable
internal data class EpisodeDetailsContextState(
    val streamings: StreamingsState = StreamingsState(),
    val user: User? = null,
    val error: Exception? = null,
) {
    @Immutable
    internal data class StreamingsState(
        val slug: SlugId? = null,
        val service: StreamingService? = null,
        val noServices: Boolean = false,
        val loading: Boolean = false,
    )
}
