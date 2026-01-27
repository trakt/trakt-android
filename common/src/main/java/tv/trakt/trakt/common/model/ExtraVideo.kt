package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.ExtraVideoDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Immutable
data class ExtraVideo(
    val title: String,
    val url: String,
    val site: String,
    val type: String,
    val official: Boolean,
    val publishedAt: ZonedDateTime,
) {
    val getYoutubeImageUrl: String
        get() = "https://img.youtube.com/vi/${url.substringAfterLast("v=")}/hqdefault.jpg"

    companion object {
        fun fromDto(dto: ExtraVideoDto): ExtraVideo {
            return ExtraVideo(
                title = dto.title,
                url = dto.url,
                site = dto.site,
                type = dto.type,
                official = dto.official,
                publishedAt = runCatching {
                    dto.publishedAt.toZonedDateTime()
                }.getOrElse {
                    // Handle possible alternate date format: "2017-01-05 17:06:25 UTC"
                    val altFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    runCatching {
                        ZonedDateTime.parse(dto.publishedAt, altFormat)
                    }.getOrDefault(nowUtc())
                },
            )
        }
    }
}
