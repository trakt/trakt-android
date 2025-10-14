package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.SeasonDto
import java.time.ZonedDateTime

@Immutable
data class Season(
    val ids: Ids,
    val number: Int,
    val episodeCount: Int?,
    val images: Images?,
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
        episodeCount = dto.episodeCount,
        images = dto.images?.let {
            Images(poster = it.poster.toImmutableList())
        },
        firstAired = dto.firstAired?.toZonedDateTime(),
        updatedAt = dto.updatedAt?.toZonedDateTime(),
    )
}
