package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.SeasonDto
import tv.trakt.trakt.common.networking.SeasonLikesDto
import tv.trakt.trakt.common.networking.SeasonRatingDto
import java.time.ZonedDateTime

@Immutable
data class Season(
    val ids: Ids,
    val number: Int,
    val rating: Rating,
    val episodeCount: Int?,
    val images: Images?,
    val overview: String?,
    val firstAired: ZonedDateTime?,
    val updatedAt: ZonedDateTime?,
) {
    companion object

    val isSpecial: Boolean
        get() = number == 0
}

fun Season.Companion.fromDto(dto: SeasonDto): Season {
    return Season(
        ids = Ids(
            trakt = dto.ids.trakt.toTraktId(),
            slug = "".toSlugId(),
        ),
        number = dto.number,
        rating = Rating(
            rating = dto.rating ?: 0f,
            votes = dto.votes ?: 0,
        ),
        episodeCount = dto.episodeCount,
        images = dto.images?.let {
            Images(poster = it.poster.toImmutableList())
        },
        overview = dto.overview,
        firstAired = dto.firstAired?.toZonedDateTime(),
        updatedAt = dto.updatedAt?.toZonedDateTime(),
    )
}

fun Season.Companion.fromDto(dto: SeasonLikesDto): Season {
    return Season(
        ids = Ids(
            trakt = dto.ids.trakt.toTraktId(),
            slug = "".toSlugId(),
        ),
        number = dto.number,
        rating = Rating(
            rating = dto.rating ?: 0f,
            votes = dto.votes ?: 0,
        ),
        episodeCount = dto.episodeCount,
        images = dto.images?.let {
            Images(poster = it.poster.toImmutableList())
        },
        overview = dto.overview,
        firstAired = dto.firstAired?.toZonedDateTime(),
        updatedAt = dto.updatedAt?.toZonedDateTime(),
    )
}

fun Season.Companion.fromDto(dto: SeasonRatingDto): Season {
    return Season(
        ids = Ids(
            trakt = dto.ids.trakt.toTraktId(),
            slug = "".toSlugId(),
        ),
        number = dto.number,
        rating = Rating(0F, 0),
        episodeCount = dto.airedEpisodes,
        images = null,
        overview = null,
        firstAired = null,
        updatedAt = null,
    )
}
