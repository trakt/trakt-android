package tv.trakt.trakt.common.core.translations.model

import tv.trakt.trakt.common.core.translations.model.MediaTranslation.Companion
import tv.trakt.trakt.common.networking.EpisodeTranslationDto
import tv.trakt.trakt.common.networking.TranslationDto
import java.util.Locale

data class MediaTranslation(
    val title: String?,
    val overview: String?,
    val locale: Locale,
) {
    companion object
}

fun Companion.fromDto(dto: TranslationDto): MediaTranslation {
    return MediaTranslation(
        title = dto.title.orEmpty().ifEmpty { null },
        overview = dto.overview.orEmpty().ifEmpty { null },
        locale = Locale.Builder()
            .setLanguage(dto.language)
            .setRegion(dto.country)
            .build(),
    )
}

fun Companion.fromDto(dto: EpisodeTranslationDto): MediaTranslation {
    return MediaTranslation(
        title = dto.title.orEmpty().ifEmpty { null },
        overview = dto.overview.orEmpty().ifEmpty { null },
        locale = Locale.Builder()
            .setLanguage(dto.language)
            .setRegion(dto.country)
            .build(),
    )
}
