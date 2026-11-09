package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer

private val imageSizeRegex = Regex("/(thumb|medium|original|full)/")

@Immutable
@Serializable
data class Images(
    @Serializable(with = ImmutableListSerializer::class)
    val fanart: ImmutableList<String> = EmptyImmutableList,
    @Serializable(with = ImmutableListSerializer::class)
    val poster: ImmutableList<String> = EmptyImmutableList,
    @Serializable(with = ImmutableListSerializer::class)
    val posters: ImmutableList<String> = EmptyImmutableList,
    @Serializable(with = ImmutableListSerializer::class)
    val logo: ImmutableList<String> = EmptyImmutableList,
    @Serializable(with = ImmutableListSerializer::class)
    val thumb: ImmutableList<String> = EmptyImmutableList,
    @Serializable(with = ImmutableListSerializer::class)
    val headshot: ImmutableList<String> = EmptyImmutableList,
    @Serializable(with = ImmutableListSerializer::class)
    val screenshot: ImmutableList<String> = EmptyImmutableList,
) {
    fun getFanartUrl(size: Size = Size.MEDIUM): String? {
        return fanart.firstOrNull()?.let {
            buildString {
                if (!it.startsWith("https://")) {
                    append("https://")
                }
                append(it)
            }.replace(imageSizeRegex, "/${size.value}/")
        }
    }

    fun getPosterUrl(size: Size = Size.THUMB): String? {
        return poster.firstOrNull()?.let {
            buildString {
                if (!it.startsWith("https://")) {
                    append("https://")
                }
                append(it)
            }.replace(imageSizeRegex, "/${size.value}/")
        }
    }

    fun getPostersUrl(size: Size = Size.THUMB): List<String> {
        return posters
            .filter { !it.contains("placeholder") }
            .map {
                buildString {
                    if (!it.startsWith("https://")) {
                        append("https://")
                    }
                    append(it)
                }.replace(imageSizeRegex, "/${size.value}/")
            }
    }

    fun getHeadshotUrl(size: Size = Size.MEDIUM): String? {
        return headshot.firstOrNull()?.let {
            buildString {
                if (!it.startsWith("https://")) {
                    append("https://")
                }
                append(it)
            }
                .replace(imageSizeRegex, "/${size.value}/")
        }
    }

    fun getScreenshotUrl(size: Size = Size.MEDIUM): String? {
        return screenshot.firstOrNull()?.let {
            buildString {
                if (!it.startsWith("https://")) {
                    append("https://")
                }
                append(it)
            }.replace(imageSizeRegex, "/${size.value}/")
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
        fun forMovie(id: TraktId): Images = forTitle(type = "movies", id = id)

        fun forShow(id: TraktId): Images = forTitle(type = "shows", id = id)

        fun forSeason(id: TraktId): Images {
            return Images(
                poster = mediaUrls("seasons", id, "poster"),
                thumb = mediaUrls("seasons", id, "thumb"),
            )
        }

        fun forEpisode(id: TraktId): Images {
            return Images(
                screenshot = mediaUrls("episodes", id, "screenshot"),
            )
        }

        fun forPerson(id: TraktId): Images {
            return Images(
                headshot = mediaUrls("people", id, "headshot"),
                fanart = mediaUrls("people", id, "fanart"),
            )
        }

        private fun forTitle(
            type: String,
            id: TraktId,
        ): Images {
            return Images(
                fanart = mediaUrls(type, id, "fanart"),
                poster = mediaUrls(type, id, "poster"),
                logo = mediaUrls(type, id, "logo"),
                thumb = mediaUrls(type, id, "thumb"),
            )
        }

        private fun mediaUrls(
            type: String,
            id: TraktId,
            slot: String,
        ): ImmutableList<String> {
            return persistentListOf("${Config.MEDIA_BASE_URL}$type/${id.value}/${Size.MEDIUM.value}/$slot.webp")
        }
    }
}
