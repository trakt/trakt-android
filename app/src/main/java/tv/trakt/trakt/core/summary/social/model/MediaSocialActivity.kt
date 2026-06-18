package tv.trakt.trakt.core.summary.social.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.api.v3.model.V3MediaSocialResponse
import java.time.Instant

@Immutable
internal data class MediaSocialActivity(
    val type: MediaType,
    val user: User,
    val watched: Watched?,
    val watchlisted: Watchlisted?,
) {
    val lastActivityAt: Instant
        get() {
            val watchedAt = watched?.lastWatchedAt ?: Instant.MIN
            val watchlistedAt = watchlisted?.listedAt ?: Instant.MIN
            val ratedAt = watched?.rated?.ratedAt ?: Instant.MIN
            val commentedAt = watched?.commented?.createdAt ?: Instant.MIN
            return maxOf(watchedAt, watchlistedAt, ratedAt, commentedAt)
        }

    @Immutable
    data class Watched(
        val lastWatchedAt: Instant,
        val lastUpdatedAt: Instant?,
        val plays: Int,
        val rated: Rated?,
        val commented: Commented?,
    ) {
        @Immutable
        data class Rated(
            val rating: Int,
            val ratedAt: Instant,
        )

        @Immutable
        data class Commented(
            val id: TraktId,
            val comment: String,
            val spoiler: Boolean,
            val review: Boolean,
            val createdAt: Instant,
            val updatedAt: Instant,
        )
    }

    @Immutable
    data class Watchlisted(
        val listedAt: Instant,
    )

    companion object {
        fun fromDto(
            dto: V3MediaSocialResponse,
            type: MediaType,
        ): MediaSocialActivity {
            return MediaSocialActivity(
                type = type,
                user = User.fromDto(dto.user),
                watched = dto.watched?.let { watched ->
                    Watched(
                        lastWatchedAt = watched.lastWatchedAt.toInstant(),
                        lastUpdatedAt = watched.lastUpdatedAt?.toInstant(),
                        plays = watched.plays,
                        rated = watched.rated?.let { rated ->
                            Watched.Rated(
                                rating = rated.rating,
                                ratedAt = rated.ratedAt.toInstant(),
                            )
                        },
                        commented = watched.commented?.let { commented ->
                            Watched.Commented(
                                id = commented.id.trakt.toTraktId(),
                                comment = commented.comment,
                                spoiler = commented.spoiler,
                                review = commented.review,
                                createdAt = commented.createdAt.toInstant(),
                                updatedAt = commented.updatedAt.toInstant(),
                            )
                        },
                    )
                },
                watchlisted = dto.watchlisted?.let { watchlisted ->
                    Watchlisted(
                        listedAt = watchlisted.listedAt.toInstant(),
                    )
                },
            )
        }
    }
}
