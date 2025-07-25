package tv.trakt.trakt.tv.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.UserDto
import tv.trakt.trakt.common.networking.UserSettingsDto
import tv.trakt.trakt.tv.Config
import tv.trakt.trakt.tv.common.model.User.Companion

@Immutable
@Serializable
internal data class User(
    val ids: Ids,
    val username: String,
    val name: String?,
    val location: String?,
    val isPrivate: Boolean,
    val isVip: Boolean,
    val isVipEp: Boolean,
    val isVipOg: Boolean,
    val images: Images?,
    val streamings: Streamings?,
) {
    val isAnyVip: Boolean
        get() = isVip || isVipEp || isVipOg

    val hasImage: Boolean
        get() = images?.avatar?.full != null

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

internal fun Companion.fromDto(dto: UserDto): User {
    return User(
        ids = Ids(
            trakt = TraktId(dto.ids.trakt),
            slug = SlugId(dto.ids.slug ?: ""),
        ),
        username = dto.username,
        name = dto.name ?: "",
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
    )
}

internal fun Companion.fromDto(dto: UserSettingsDto): User {
    return User(
        ids = Ids(
            trakt = TraktId(dto.user.ids.trakt ?: 0),
            slug = SlugId(dto.user.ids.slug),
        ),
        username = dto.user.username,
        name = dto.user.name ?: "",
        location = dto.user.location,
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
    )
}
