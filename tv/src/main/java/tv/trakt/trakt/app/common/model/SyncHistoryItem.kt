package tv.trakt.trakt.app.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import java.time.ZonedDateTime

@Immutable
internal data class SyncHistoryItem(
    val id: Long,
    val watchedAt: ZonedDateTime,
    val type: String,
    val episode: Episode? = null,
    val show: Show? = null,
    val movie: Movie? = null,
) {
    init {
        require(type in listOf("movie", "show", "episode")) {
            "Invalid type: $type. Must be one of 'movie', 'show', or 'episode'."
        }
        when (type) {
            "episode" -> requireNotNull(episode) { "Episode must be provided when type is 'episode'." }
            "show" -> requireNotNull(show) { "Show must be provided when type is 'show'." }
            "movie" -> requireNotNull(movie) { "Movie must be provided when type is 'movie'." }
        }
    }

    val mediaCardImageUrl: String?
        get() = when (type) {
            "show" -> show?.images?.getFanartUrl()
            "movie" -> movie?.images?.getFanartUrl()
            "episode" -> episode?.images?.getScreenshotUrl()
            else -> null
        }

    val backdropImageUrl: String?
        get() = when (type) {
            "show", "episode" -> show?.images?.getFanartUrl(Images.Size.FULL)
            "movie" -> movie?.images?.getFanartUrl(Images.Size.FULL)
            else -> null
        }
}
