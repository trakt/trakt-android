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
import kotlin.time.Duration
import kotlin.time.DurationUnit.MINUTES
import kotlin.time.toDuration

@Immutable
internal data class MediaSocialActivity(
    val type: MediaType,
    val user: User,
    val watched: Watched?,
    val watchlist: Watchlist?,
) {
    val lastActivityAt: Instant
        get() {
            val watchedAt = watched?.lastWatchedAt ?: Instant.MIN
            val watchlistAt = watchlist?.listedAt ?: Instant.MIN
            val ratedAt = watched?.rated?.ratedAt ?: Instant.MIN
            val commentedAt = watched?.commented?.createdAt ?: Instant.MIN
            return maxOf(watchedAt, watchlistAt, ratedAt, commentedAt)
        }

    @Immutable
    data class Watched(
        val lastWatchedAt: Instant,
        val lastUpdatedAt: Instant?,
        val plays: Int,
        val duration: Duration?,
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
    data class Watchlist(
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
                        duration = watched.minutesWatched?.toDuration(MINUTES),
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
                watchlist = dto.watchlisted?.let {
                    Watchlist(
                        listedAt = it.listedAt.toInstant(),
                    )
                },
            )
        }
    }
}
