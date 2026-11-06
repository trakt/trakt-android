package tv.trakt.trakt.core.settings.features.history.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.networking.api.v3.model.V3WatchedPlayResponse

@Immutable
internal data class MediaPlays(
    val id: Long,
    val plays: ImmutableList<WatchedPlay>,
) {
    companion object {
        fun fromDto(
            id: String,
            plays: List<V3WatchedPlayResponse>,
        ): MediaPlays? {
            return MediaPlays(
                id = id.toLongOrNull() ?: return null,
                plays = plays.map(WatchedPlay::fromDto).toImmutableList(),
            )
        }
    }
}
