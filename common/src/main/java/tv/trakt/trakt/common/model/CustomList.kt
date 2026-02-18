package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.helpers.serializers.ZonedDateTimeSerializer
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.SearchListDto
import java.time.ZonedDateTime

@Immutable
@Serializable
data class CustomList(
    val ids: Ids,
    val name: String,
    val description: String?,
    val privacy: String?,
    val shareLink: String?,
    val type: Type?,
    val displayNumbers: Boolean?,
    val allowComments: Boolean?,
    @Serializable(ZonedDateTimeSerializer::class)
    val createdAt: ZonedDateTime,
    @Serializable(ZonedDateTimeSerializer::class)
    val updatedAt: ZonedDateTime,
    val likes: Int?,
    val images: Images?,
    val user: User,
) {
    @Serializable
    enum class Type(
        val value: String,
    ) {
        ALL("all"),
        PERSONAL("personal"),
        OFFICIAL("official"),
        WATCHLIST("watchlist"),
        FAVORITES("favorites"),
        ;

        companion object {
            fun fromString(value: String): Type? {
                return entries.find { it.value == value }
            }
        }
    }

    companion object {
        fun fromDto(dto: ListDto): CustomList {
            return CustomList(
                ids = Ids(
                    trakt = TraktId(dto.ids.trakt),
                    slug = SlugId(dto.ids.slug),
                ),
                name = dto.name,
                description = dto.description,
                privacy = dto.privacy,
                shareLink = dto.shareLink,
                type = Type.fromString(dto.type.lowercase()),
                displayNumbers = dto.displayNumbers,
                allowComments = dto.allowComments,
                createdAt = dto.createdAt.toZonedDateTime(),
                updatedAt = dto.updatedAt.toZonedDateTime(),
                likes = dto.likes,
                images = dto.images?.let {
                    Images(
                        posters = it.posters.toImmutableList(),
                    )
                },
                user = User.fromDto(dto.user),
            )
        }

        fun fromDto(dto: SearchListDto) =
            CustomList(
                ids = Ids(
                    trakt = TraktId(dto.ids.trakt),
                    slug = SlugId(dto.ids.slug),
                ),
                name = dto.name,
                description = dto.description,
                privacy = dto.privacy,
                shareLink = dto.shareLink,
                type = Type.fromString(dto.type.lowercase()),
                displayNumbers = dto.displayNumbers,
                allowComments = dto.allowComments,
                createdAt = dto.createdAt.toZonedDateTime(),
                updatedAt = dto.updatedAt.toZonedDateTime(),
                likes = dto.likes,
                images = dto.images?.let {
                    Images(
                        posters = it.posters.toImmutableList(),
                    )
                },
                user = User.fromDto(dto.user),
            )
    }
}
