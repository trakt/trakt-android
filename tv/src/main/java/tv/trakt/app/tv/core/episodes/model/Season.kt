package tv.trakt.app.tv.core.episodes.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.common.model.Ids
import tv.trakt.app.tv.common.model.Images
import tv.trakt.app.tv.common.model.toSlugId
import tv.trakt.app.tv.common.model.toTraktId
import tv.trakt.app.tv.helpers.extensions.toZonedDateTime
import tv.trakt.app.tv.networking.openapi.SeasonDto
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
