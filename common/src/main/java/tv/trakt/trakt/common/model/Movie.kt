package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Movie.Companion
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Immutable
@kotlinx.serialization.Serializable
data class Movie(
    val ids: Ids,
    val title: String,
    val overview: String?,
    @kotlinx.serialization.Serializable(LocalDateSerializer::class)
    val released: LocalDate?,
    val year: Int?,
    @Serializable(ImmutableListSerializer::class)
    val genres: ImmutableList<String>,
    val images: Images?,
    val colors: MediaColors?,
    val rating: Rating,
    val certification: String?,
    val runtime: Duration?,
) {
    companion object
}

fun Companion.fromDto(dto: MovieDto): Movie {
    return Movie(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        overview = dto.overview,
        year = dto.year,
        released = dto.released?.let { LocalDate.parse(it) },
        genres = (dto.genres ?: listOf()).toImmutableList(),
        images = dto.images?.let { Images.fromDto(it) },
        colors = dto.colors?.poster?.let {
            MediaColors(
                Pair(
                    Color(it.getOrElse(0) { "#00000000" }.toColorInt()),
                    Color(it.getOrElse(1) { "#00000000" }.toColorInt()),
                ),
            )
        },
        certification = dto.certification,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        runtime = dto.runtime?.minutes,
    )
}

fun Companion.fromDto(dto: RecommendedMovieDto): Movie {
    return Movie(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        overview = dto.overview,
        year = dto.year,
        released = dto.released?.let { LocalDate.parse(it) },
        genres = (dto.genres ?: listOf()).toImmutableList(),
        images = dto.images?.let { Images.fromDto(it) },
        colors = dto.colors?.poster?.let {
            MediaColors(
                Pair(
                    Color(it.getOrElse(0) { "#00000000" }.toColorInt()),
                    Color(it.getOrElse(1) { "#00000000" }.toColorInt()),
                ),
            )
        },
        certification = dto.certification,
        rating = Rating(
            rating = dto.rating ?: 0F,
            votes = dto.votes ?: 0,
        ),
        runtime = dto.runtime?.minutes,
    )
}
