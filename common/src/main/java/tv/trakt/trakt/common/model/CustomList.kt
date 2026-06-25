package tv.trakt.trakt.common.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.helpers.serializers.ZonedDateTimeSerializer
import tv.trakt.trakt.common.networking.LikedListDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.SearchListDto
import tv.trakt.trakt.resources.R
import java.time.ZonedDateTime

@Immutable
@Serializable
data class CustomList(
    val ids: Ids,
    val name: String,
    val description: String?,
    val privacy: Privacy?,
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
        All("all"),
        List("list"),
        Personal("personal"),
        Official("official"),
        Watchlist("watchlist"),
        Favorites("favorites"),
        ;

        companion object {
            fun fromString(value: String?): Type? {
                return entries.find { it.value == value }
            }
        }
    }

    @Serializable
    enum class Privacy(
        val value: String,
        @param:StringRes val displayRes: Int,
        @param:StringRes val displayInfoRes: Int,
    ) {
        Public("public", R.string.text_list_public, R.string.text_list_public_info),
        Private("private", R.string.text_list_private, R.string.text_list_private_info),
        ;

        companion object {
            fun fromString(value: String?): Privacy? {
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
                privacy = Privacy.fromString(dto.privacy),
                shareLink = dto.shareLink,
                type = Type.fromString(dto.type.lowercase()),
                displayNumbers = dto.displayNumbers,
                allowComments = dto.allowComments,
                createdAt = dto.createdAt.toZonedDateTime(),
                updatedAt = dto.updatedAt.toZonedDateTime(),
                likes = dto.likes,
                images = dto.images?.let {
                    Images(
                        posters = it.posters
                            .distinct()
                            .toImmutableList(),
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
                privacy = Privacy.fromString(dto.privacy),
                shareLink = dto.shareLink,
                type = Type.fromString(dto.type.lowercase()),
                displayNumbers = dto.displayNumbers,
                allowComments = dto.allowComments,
                createdAt = dto.createdAt.toZonedDateTime(),
                updatedAt = dto.updatedAt.toZonedDateTime(),
                likes = dto.likes,
                images = dto.images?.let {
                    Images(
                        posters = it.posters
                            .distinct()
                            .toImmutableList(),
                    )
                },
                user = User.fromDto(dto.user),
            )

        fun fromDto(dto: LikedListDto): CustomList {
            val list = dto.list
            return CustomList(
                ids = Ids(
                    trakt = list.ids?.trakt?.toTraktId()!!,
                    slug = list.ids.slug.toSlugId(),
                ),
                name = list.name ?: "",
                description = list.description,
                privacy = Privacy.fromString(list.privacy),
                shareLink = list.shareLink,
                type = Type.fromString(list.type?.lowercase()),
                displayNumbers = list.displayNumbers,
                allowComments = list.allowComments,
                createdAt = list.createdAt?.toZonedDateTime()!!,
                updatedAt = list.updatedAt?.toZonedDateTime()!!,
                likes = list.likes,
                images = list.images?.let {
                    Images(
                        posters = it.posters
                            .distinct()
                            .toImmutableList(),
                    )
                },
                user = User.fromDto(list.user!!),
            )
        }
    }
}
