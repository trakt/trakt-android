package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.helpers.serializers.ZonedDateTimeSerializer
import tv.trakt.trakt.common.model.Show.Companion
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.ShowDto
import tv.trakt.trakt.common.networking.ShowLikesDto
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Immutable
@Serializable
data class Show(
    val ids: Ids,
    val title: String,
    val titleOriginal: String?,
    val overview: String?,
    @Serializable(ZonedDateTimeSerializer::class)
    val released: ZonedDateTime?,
    val year: Int?,
    @Serializable(ImmutableListSerializer::class)
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

fun Companion.fromDto(dto: ShowDto): Show {
    return Show(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        titleOriginal = dto.originalTitle,
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

fun Companion.fromDto(dto: RecommendedShowDto): Show {
    return Show(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        titleOriginal = dto.originalTitle,
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

fun Companion.fromDto(dto: ShowLikesDto): Show {
    return Show(
        ids = Ids.fromDto(dto.ids),
        title = dto.title,
        titleOriginal = dto.originalTitle,
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
