package tv.trakt.trakt.app.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.ExtraVideoDto
import java.time.ZonedDateTime

@Immutable
internal data class ExtraVideo(
    val title: String,
    val url: String,
    val site: String,
    val type: String,
    val official: Boolean,
    val publishedAt: ZonedDateTime,
) {
    val getYoutubeImageUrl: String
        get() = "https://img.youtube.com/vi/${url.substringAfterLast("v=")}/hqdefault.jpg"

    internal companion object {
        fun fromDto(dto: ExtraVideoDto): ExtraVideo {
            return ExtraVideo(
                title = dto.title,
                url = dto.url,
                site = dto.site,
                type = dto.type,
                official = dto.official,
                publishedAt = dto.publishedAt.toZonedDateTime(),
            )
        }
    }
}
