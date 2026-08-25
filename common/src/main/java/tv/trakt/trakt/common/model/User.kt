package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.model.User.Companion
import tv.trakt.trakt.common.networking.UserCommentsDto
import tv.trakt.trakt.common.networking.UserSettingsDto

@Immutable
@Serializable
data class User(
    val ids: Ids,
    val username: String,
    val name: String?,
    val email: String?,
    val location: String?,
    val about: String?,
    val isPrivate: Boolean,
    val isVip: Boolean,
    val isVipEp: Boolean,
    val isVipOg: Boolean,
    val images: Images?,
    val streamings: Streamings?,
    val settings: Settings?,
) {
    @Immutable
    @Serializable
    data class Settings(
        val watchOnlyOnce: Boolean,
        val ratingPrompts: Boolean,
        val coverImage: String?,
    )

    val isAnyVip: Boolean
        get() = isVip || isVipEp || isVipOg

    val hasAvatar: Boolean
        get() = images?.avatar?.full != null

    val displayName: String
        get() = (name ?: "").ifBlank { username }

    @Immutable
    @Serializable
    data class Images(
        val avatar: Image?,
    )

    @Immutable
    @Serializable
    data class Image(
        val full: String?,
    )

    @Immutable
    @Serializable
    data class Streamings(
        val country: String,
        val favorites: List<String>?,
        val isFavoritesOnly: Boolean,
    )

    companion object
}

fun Companion.fromDto(dto: UserSettingsDto): User {
    return User(
        ids = Ids(
            trakt = TraktId(dto.user.ids.trakt ?: 0),
            slug = SlugId(dto.user.ids.slug),
        ),
        username = dto.user.username,
        name = dto.user.name ?: "",
        email = dto.user.email,
        location = dto.user.location,
        about = dto.user.about,
        isPrivate = dto.user.private,
        isVip = dto.user.vip,
        isVipEp = dto.user.vipEp,
        isVipOg = dto.user.vipOg,
        images = User.Images(
            avatar = dto.user.images.avatar.let {
                User.Image(it.full)
            },
        ),
        streamings = dto.browsing?.watchnow?.let {
            User.Streamings(
                country = it.country ?: Config.DEFAULT_COUNTRY_CODE,
                favorites = it.favorites,
                isFavoritesOnly = it.onlyFavorites,
            )
        },
        settings = User.Settings(
            watchOnlyOnce = dto.browsing?.watchOnlyOnce ?: false,
            ratingPrompts = dto.browsing?.showRatingPrompt ?: false,
            coverImage = dto.user.vipCoverImage,
        ),
    )
}

fun Companion.fromDto(dto: UserCommentsDto): User {
    return User(
        ids = Ids(
            trakt = dto.ids.trakt.toTraktId(),
            slug = dto.ids.slug?.toSlugId() ?: SlugId(""),
        ),
        username = dto.username,
        name = dto.name ?: "",
        about = dto.about,
        email = null,
        location = dto.location,
        isPrivate = dto.private,
        isVip = dto.vip ?: false,
        isVipEp = dto.vipEp ?: false,
        isVipOg = dto.vipOg ?: false,
        images = User.Images(
            avatar = dto.images?.avatar?.let {
                User.Image(it.full)
            },
        ),
        streamings = null,
        settings = User.Settings(
            watchOnlyOnce = false,
            ratingPrompts = false,
            coverImage = dto.vipCoverImage,
        ),
    )
}
