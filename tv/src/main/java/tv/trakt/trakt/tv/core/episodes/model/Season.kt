package tv.trakt.trakt.tv.core.episodes.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.networking.SeasonDto
import tv.trakt.trakt.tv.common.model.Ids
import tv.trakt.trakt.tv.common.model.Images
import tv.trakt.trakt.tv.common.model.toSlugId
import tv.trakt.trakt.tv.common.model.toTraktId
import tv.trakt.trakt.tv.helpers.extensions.toZonedDateTime
import java.time.ZonedDateTime

@Immutable
internal data class Season(
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

internal fun Season.Companion.fromDto(dto: SeasonDto): Season {
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
