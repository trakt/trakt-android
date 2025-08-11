package tv.trakt.trakt.app.core.search.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.app.core.movies.model.Movie
import java.time.Instant

@Serializable
internal data class RecentMovieEntity(
    val movie: Movie,
    val createdAt: String, // Format: "2022-12-03T10:15:30Z"
) {
    companion object
}

internal fun RecentMovieEntity.Companion.create(
    movie: Movie,
    createdAt: Instant,
): RecentMovieEntity {
    return RecentMovieEntity(
        movie = movie,
        createdAt = createdAt.toString(),
    )
}
