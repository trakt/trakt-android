package tv.trakt.trakt.common.core.translations.model

import tv.trakt.trakt.common.networking.EpisodeTranslationDto
import tv.trakt.trakt.common.networking.TranslationDto

data class MediaTranslation(
    val title: String?,
    val overview: String?,
) {
    val isEmpty: Boolean
        get() = title == null && overview == null

    companion object {
        val Empty = MediaTranslation(title = null, overview = null)

        fun fromDto(dto: TranslationDto): MediaTranslation {
            return MediaTranslation(
                title = dto.title.orEmpty().ifEmpty { null },
                overview = dto.overview.orEmpty().ifEmpty { null },
            )
        }

        fun fromDto(dto: EpisodeTranslationDto): MediaTranslation {
            return MediaTranslation(
                title = dto.title.orEmpty().ifEmpty { null },
                overview = dto.overview.orEmpty().ifEmpty { null },
            )
        }
    }
}
