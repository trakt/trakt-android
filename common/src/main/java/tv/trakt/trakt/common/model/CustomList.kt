package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.ListDto
import java.time.ZonedDateTime

@Immutable
data class CustomList(
    val ids: Ids,
    val name: String,
    val description: String?,
    val privacy: String?,
    val shareLink: String?,
    val type: Type?,
    val displayNumbers: Boolean?,
    val allowComments: Boolean?,
//    val sortType: SortType?,
//    val sortOrder: SortOrder?,
    val createdAt: ZonedDateTime,
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
//                sortType = SortType.fromString(dto.sortBy),
//                sortOrder = SortOrder.fromString(dto.sortHow.value),
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
}
