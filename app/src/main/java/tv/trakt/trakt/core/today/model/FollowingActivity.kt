package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.api.v3.model.V3FollowingActivityResponse
import java.time.Instant

@Immutable
internal data class FollowingActivity(
    val key: String,
    val activityAt: Instant,
    val user: User,
    val target: Target,
    val detail: Detail,
) {
    @Immutable
    sealed interface Target {
        val media: TodayMedia

        @Immutable
        data class MovieTarget(
            val movie: Movie,
        ) : Target {
            override val media = TodayMedia.MovieMedia(movie)
        }

        @Immutable
        data class ShowTarget(
            val show: Show,
        ) : Target {
            override val media = TodayMedia.ShowMedia(show)
        }

        @Immutable
        data class SeasonTarget(
            val show: Show,
            val season: Season,
        ) : Target {
            override val media = TodayMedia.ShowMedia(show)
        }

        @Immutable
        data class EpisodeTarget(
            val show: Show,
            val episode: Episode,
        ) : Target {
            override val media = TodayMedia.ShowMedia(show)
        }
    }

    @Immutable
    sealed interface Detail {
        @Immutable
        data object Watch : Detail

        @Immutable
        data class Rating(
            val rating: Int,
        ) : Detail

        @Immutable
        data class Comment(
            val id: Long,
            val text: String,
            val gifUrl: String?,
            val isSpoiler: Boolean,
            val isReview: Boolean,
            val likeCount: Int,
            val replyCount: Int,
        ) : Detail
    }

    companion object {
        /**
         * Returns null for entries the app cannot render: unknown actions or targets,
         * or targets missing the media they point at.
         */
        fun fromDto(dto: V3FollowingActivityResponse): FollowingActivity? {
            val user = dto.user ?: return null
            val activityAt = dto.activityAt ?: return null
            val action = dto.action ?: return null
            val target = targetFromDto(dto) ?: return null
            val detail = detailFromDto(dto) ?: return null

            return FollowingActivity(
                key = "$action:${dto.id}",
                activityAt = activityAt.toInstant(),
                user = User.fromDto(user),
                target = target,
                detail = detail,
            )
        }

        private fun targetFromDto(dto: V3FollowingActivityResponse): Target? {
            val show = dto.show?.let { Show.fromDto(it) }

            return when (dto.type) {
                "movie" -> {
                    dto.movie?.let { Target.MovieTarget(Movie.fromDto(it)) }
                }
                "show" -> {
                    show?.let { Target.ShowTarget(it) }
                }
                "season" -> {
                    val season = dto.season ?: return null
                    show?.let { Target.SeasonTarget(it, Season.fromDto(season)) }
                }
                "episode" -> {
                    val episode = dto.episode ?: return null
                    show?.let { Target.EpisodeTarget(it, Episode.fromDto(episode)) }
                }
                else -> {
                    null
                }
            }
        }

        private fun detailFromDto(dto: V3FollowingActivityResponse): Detail? {
            return when (dto.action) {
                "watch" -> Detail.Watch
                "rating" -> dto.rating?.let { Detail.Rating(it) }
                "comment" -> dto.comment?.let {
                    Detail.Comment(
                        id = it.id,
                        text = it.comment,
                        gifUrl = it.gif?.url,
                        isSpoiler = it.spoiler,
                        isReview = it.review,
                        likeCount = it.likes,
                        replyCount = it.replies,
                    )
                }
                else -> null
            }
        }
    }
}
