package tv.trakt.trakt.core.settings.features.history.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.networking.api.v3.model.V3WatchedPlayResponse
import kotlin.time.Instant

@Immutable
internal data class WatchedPlay(
    val id: Long,
    val watchedAt: Instant,
) {
    companion object {
        fun fromDto(dto: V3WatchedPlayResponse): WatchedPlay {
            return WatchedPlay(
                id = dto.id,
                watchedAt = Instant.parseOrNull(dto.watchedAt) ?: Instant.DISTANT_PAST,
            )
        }
    }
}
