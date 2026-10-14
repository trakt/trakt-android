package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.EpisodeDto
import tv.trakt.trakt.common.networking.MovieDto
import tv.trakt.trakt.common.networking.SeasonDto
import tv.trakt.trakt.common.networking.ShowDto
import tv.trakt.trakt.common.networking.UserCommentsDto

/**
 * Entry of `/v3/users/me/following/activities`. Every field past [id] is optional so a single
 * entry of an unknown action does not fail decoding of the whole page.
 */
@Immutable
@Serializable
data class V3FollowingActivityResponse(
    val id: Long,
    val action: String? = null,
    @SerialName("activity_at")
    val activityAt: String? = null,
    val type: String? = null,
    val user: UserCommentsDto? = null,
    val movie: MovieDto? = null,
    val show: ShowDto? = null,
    val season: SeasonDto? = null,
    val episode: EpisodeDto? = null,
    val rating: Int? = null,
    val comment: Comment? = null,
) {
    @Immutable
    @Serializable
    data class Comment(
        val id: Long,
        val comment: String,
        val gif: Gif? = null,
        val spoiler: Boolean = false,
        val review: Boolean = false,
        val likes: Int = 0,
        val replies: Int = 0,
    )

    @Immutable
    @Serializable
    data class Gif(
        val url: String,
        val width: Int? = null,
        val height: Int? = null,
    )
}
