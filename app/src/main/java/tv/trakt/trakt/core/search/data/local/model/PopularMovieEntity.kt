package tv.trakt.trakt.core.search.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Movie
import java.time.Instant

@Serializable
internal data class PopularMovieEntity(
    val movie: Movie,
    val rank: Int,
    val createdAt: String,
) {
    companion object
}

internal fun PopularMovieEntity.Companion.create(
    movie: Movie,
    rank: Int,
    createdAt: Instant,
): PopularMovieEntity {
    return PopularMovieEntity(
        movie = movie,
        rank = rank,
        createdAt = createdAt.toString(),
    )
}
