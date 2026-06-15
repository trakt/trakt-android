package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.networking.UserMediaDto

@Immutable
@Serializable
data class V3MediaSocialResponse(
    val user: UserMediaDto,
    @SerialName("followed_at")
    val followedAt: String,
    val watched: Watched?,
    val watchlisted: Watchlisted?,
) {
    @Immutable
    @Serializable
    data class Watched(
        @SerialName("last_watched_at")
        val lastWatchedAt: String,
        @SerialName("last_updated_at")
        val lastUpdatedAt: String?,
        val plays: Int,
        @SerialName("rating")
        val rated: Rated?,
        @SerialName("comment")
        val commented: Commented?,
    ) {
        @Immutable
        @Serializable
        data class Rated(
            val rating: Int,
            @SerialName("rated_at")
            val ratedAt: String,
        )

        @Immutable
        @Serializable
        data class Commented(
            @SerialName("ids")
            val id: CommentedId,
            val comment: String,
            val spoiler: Boolean,
            val review: Boolean,
            @SerialName("created_at")
            val createdAt: String,
            @SerialName("updated_at")
            val updatedAt: String,
        ) {
            @Immutable
            @Serializable
            data class CommentedId(
                val trakt: Int,
            )
        }
    }

    @Immutable
    @Serializable
    data class Watchlisted(
        @SerialName("listed_at")
        val listedAt: String,
    )
}
