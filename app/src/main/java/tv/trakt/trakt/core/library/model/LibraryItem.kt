package tv.trakt.trakt.core.library.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

@Immutable
internal sealed class LibraryItem(
    open val collectedAt: Instant,
    open val updatedAt: Instant,
    open val availableOn: ImmutableList<String>,
) {
    @Immutable
    internal data class MovieItem(
        val movie: Movie,
        override val collectedAt: Instant,
        override val updatedAt: Instant,
        override val availableOn: ImmutableList<String>,
    ) : LibraryItem(collectedAt, updatedAt, availableOn)

    @Immutable
    internal data class EpisodeItem(
        val episode: Episode,
        val show: Show,
        override val collectedAt: Instant,
        override val updatedAt: Instant,
        override val availableOn: ImmutableList<String>,
    ) : LibraryItem(collectedAt, updatedAt, availableOn)

    val type: MediaType
        get() = when (this) {
            is MovieItem -> MediaType.MOVIE
            is EpisodeItem -> MediaType.EPISODE
        }

    val id: TraktId
        get() = when (this) {
            is MovieItem -> movie.ids.trakt
            is EpisodeItem -> episode.ids.trakt
        }

    val key: String
        get() = when (this) {
            is MovieItem -> "${id.value}-${type.value}"
            is EpisodeItem -> "${id.value}-${type.value}"
        }
}
