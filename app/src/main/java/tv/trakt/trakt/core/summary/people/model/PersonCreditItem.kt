package tv.trakt.trakt.core.summary.people.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import kotlin.time.Duration

@Immutable
internal sealed class PersonCreditItem(
    open val credit: String?,
) {
    @Immutable
    internal data class MovieItem(
        val movie: Movie,
        override val credit: String?,
    ) : PersonCreditItem(credit)

    @Immutable
    internal data class ShowItem(
        val show: Show,
        override val credit: String?,
    ) : PersonCreditItem(credit)

    val id: TraktId
        get() = when (this) {
            is ShowItem -> show.ids.trakt
            is MovieItem -> movie.ids.trakt
        }

    val key: String
        get() = when (this) {
            is ShowItem -> "${show.ids.trakt.value}-show"
            is MovieItem -> "${movie.ids.trakt.value}-movie"
        }

    val type: MediaType
        get() = when (this) {
            is ShowItem -> MediaType.SHOW
            is MovieItem -> MediaType.MOVIE
        }

    val title: String
        get() = when (this) {
            is ShowItem -> show.title
            is MovieItem -> movie.title
        }

    val images: Images?
        get() = when (this) {
            is ShowItem -> show.images
            is MovieItem -> movie.images
        }

    val rating: Rating
        get() = when (this) {
            is ShowItem -> show.rating
            is MovieItem -> movie.rating
        }

    val runtime: Duration?
        get() = when (this) {
            is ShowItem -> show.runtime
            is MovieItem -> movie.runtime
        }

    val released: ZonedDateTime?
        get() = when (this) {
            is ShowItem -> show.released
            is MovieItem -> movie.released?.atStartOfDay(UTC)
        }
}
