package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.networking.ImagesDto

private val emptyStringList = emptyList<String>().toImmutableList()

@Immutable
@Serializable
data class Images(
    @Serializable(with = ImmutableListSerializer::class)
    val fanart: ImmutableList<String> = emptyStringList,
    @Serializable(with = ImmutableListSerializer::class)
    val poster: ImmutableList<String> = emptyStringList,
    @Serializable(with = ImmutableListSerializer::class)
    val posters: ImmutableList<String> = emptyStringList,
    @Serializable(with = ImmutableListSerializer::class)
    val logo: ImmutableList<String> = emptyStringList,
    @Serializable(with = ImmutableListSerializer::class)
    val thumb: ImmutableList<String> = emptyStringList,
    @Serializable(with = ImmutableListSerializer::class)
    val headshot: ImmutableList<String> = emptyStringList,
    @Serializable(with = ImmutableListSerializer::class)
    val screenshot: ImmutableList<String> = emptyStringList,
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
