package tv.trakt.trakt.core.comments.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed interface MentionSource {
    data class Movie(
        val movieId: TraktId,
    ) : MentionSource

    data class Show(
        val showId: TraktId,
    ) : MentionSource

    data class Episode(
        val showId: TraktId,
        val season: Int,
        val episode: Int,
    ) : MentionSource
}
