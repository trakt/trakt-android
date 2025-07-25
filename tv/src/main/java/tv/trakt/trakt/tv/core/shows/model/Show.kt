package tv.trakt.trakt.tv.core.shows.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.ShowDto
import tv.trakt.trakt.tv.common.model.Ids
import tv.trakt.trakt.tv.common.model.Images
import tv.trakt.trakt.tv.common.model.MediaColors
import tv.trakt.trakt.tv.common.model.Rating
import tv.trakt.trakt.tv.core.shows.model.Show.Companion
import tv.trakt.trakt.tv.helpers.extensions.toZonedDateTime
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Immutable
internal data class Show(
    val ids: Ids,
    val title: String,
    val overview: String?,
    val released: ZonedDateTime?,
    val year: Int?,
    val genres: ImmutableList<String>,
    val images: Images?,
    val colors: MediaColors?,
    val rating: Rating,
    val certification: String?,
    val runtime: Duration?,
    val airedEpisodes: Int,
) {
    companion object
}

internal fun Companion.fromDto(dto: ShowDto): Show {
    return Show(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        overview = dto.overview,
        year = dto.year,
        released = dto.firstAired?.toZonedDateTime(),
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
        airedEpisodes = dto.airedEpisodes ?: 0,
    )
}

internal fun Companion.fromDto(dto: RecommendedShowDto): Show {
    return Show(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        overview = dto.overview,
        year = dto.year,
        released = dto.firstAired?.toZonedDateTime(),
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
        airedEpisodes = dto.airedEpisodes ?: 0,
    )
}
