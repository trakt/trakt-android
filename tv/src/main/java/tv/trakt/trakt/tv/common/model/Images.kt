package tv.trakt.trakt.tv.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.networking.openapi.ImagesDto

@Immutable
internal data class Images(
    val fanart: ImmutableList<String> = emptyList<String>().toImmutableList(),
    val poster: ImmutableList<String> = emptyList<String>().toImmutableList(),
    val posters: ImmutableList<String> = emptyList<String>().toImmutableList(),
    val logo: ImmutableList<String> = emptyList<String>().toImmutableList(),
    val thumb: ImmutableList<String> = emptyList<String>().toImmutableList(),
    val headshot: ImmutableList<String> = emptyList<String>().toImmutableList(),
    val screenshot: ImmutableList<String> = emptyList<String>().toImmutableList(),
) {
    fun getFanartUrl(size: Size = Size.MEDIUM): String? {
        return fanart.firstOrNull()?.let {
            buildString {
                if (!it.startsWith("https://")) {
                    append("https://")
                }
                append(it)
            }.replace("/medium/", "/${size.value}/")
        }
    }

    fun getPosterUrl(size: Size = Size.THUMB): String? {
        return poster.firstOrNull()?.let {
            buildString {
                if (!it.startsWith("https://")) {
                    append("https://")
                }
                append(it)
            }.replace("/thumb/", "/${size.value}/")
        }
    }

    fun getPostersUrl(size: Size = Size.THUMB): List<String>? {
        return posters
            .filter { !it.contains("placeholder") }
            .map {
                buildString {
                    if (!it.startsWith("https://")) {
                        append("https://")
                    }
                    append(it)
                }.replace("/thumb/", "/${size.value}/")
            }
    }

    fun getHeadshotUrl(size: Size = Size.MEDIUM): String? {
        return headshot.firstOrNull()?.let {
            buildString {
                if (!it.startsWith("https://")) {
                    append("https://")
                }
                append(it)
            }.replace("/thumb/", "/${size.value}/")
        }
    }

    fun getLogoUrl(size: Size = Size.MEDIUM): String? {
        return logo.firstOrNull()?.let {
            buildString {
                if (!it.startsWith("https://")) {
                    append("https://")
                }
                append(it)
            }.replace("/medium/", "/${size.value}/")
        }
    }

    fun getScreenshotUrl(size: Size = Size.MEDIUM): String? {
        return screenshot.firstOrNull()?.let {
            buildString {
                if (!it.startsWith("https://")) {
                    append("https://")
                }
                append(it)
            }.replace("/medium/", "/${size.value}/")
        }
    }

    enum class Size(
        val value: String,
    ) {
        THUMB("thumb"),
        MEDIUM("medium"),
        FULL("full"),
    }

    companion object {
        fun fromDto(dto: ImagesDto): Images {
            return Images(
                fanart = dto.fanart.toImmutableList(),
                poster = dto.poster.toImmutableList(),
                thumb = dto.thumb.toImmutableList(),
                logo = dto.logo.toImmutableList(),
            )
        }
    }
}
