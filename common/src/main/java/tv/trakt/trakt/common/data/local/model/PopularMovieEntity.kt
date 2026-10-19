package tv.trakt.trakt.common.data.local.model

import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Movie
import java.time.Instant

@Serializable
data class PopularMovieEntity(
    val movie: Movie,
    val rank: Int,
    val createdAt: String,
) {
    companion object
}

fun PopularMovieEntity.Companion.create(
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
