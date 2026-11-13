package tv.trakt.trakt.app.core.lists.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.ratings.UserRating
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration

@Immutable
internal data class ListSortValues(
    val listedAt: Instant?,
    val released: LocalDate?,
    val runtime: Duration?,
    val rating: Rating,
    val userRating: UserRating?,
) {
    companion object {
        fun of(
            show: Show,
            listedAt: Instant?,
            userRating: UserRating?,
        ): ListSortValues {
            return ListSortValues(
                listedAt = listedAt,
                released = show.releasedAt?.toLocalDay(),
                runtime = show.totalRuntime,
                rating = show.rating,
                userRating = userRating,
            )
        }

        fun of(
            movie: Movie,
            listedAt: Instant?,
            userRating: UserRating?,
        ): ListSortValues {
            return ListSortValues(
                listedAt = listedAt,
                released = movie.released,
                runtime = movie.runtime,
                rating = movie.rating,
                userRating = userRating,
            )
        }
    }
}
