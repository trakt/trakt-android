package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.today.model.FollowingActivity.Detail
import tv.trakt.trakt.core.today.model.FollowingActivity.Target
import java.time.Instant

/**
 * A single friend action on a title. Consecutive episode watches by the same friend are
 * merged into one action, with [episodeCount] holding how many were merged.
 */
@Immutable
internal data class TodayFriendAction(
    val key: String,
    val user: User,
    val activityAt: Instant,
    val target: Target,
    val detail: Detail,
    val episodeCount: Int,
    val milestone: TodayMilestone?,
) {
    val episode: Episode?
        get() = (target as? Target.EpisodeTarget)?.episode

    val season: Season?
        get() = (target as? Target.SeasonTarget)?.season

    val rating: Int?
        get() = (detail as? Detail.Rating)?.rating

    val comment: Detail.Comment?
        get() = detail as? Detail.Comment

    val isEpisodeWatch: Boolean
        get() = detail is Detail.Watch && target is Target.EpisodeTarget

    /** Rating given to the movie or show itself, not to a season or episode. */
    val isTitleRating: Boolean
        get() = detail is Detail.Rating &&
            (target is Target.MovieTarget || target is Target.ShowTarget)

    companion object {
        fun fromActivity(activity: FollowingActivity): TodayFriendAction {
            val episode = (activity.target as? Target.EpisodeTarget)?.episode
            val isWatch = activity.detail is Detail.Watch

            return TodayFriendAction(
                key = activity.key,
                user = activity.user,
                activityAt = activity.activityAt,
                target = activity.target,
                detail = activity.detail,
                episodeCount = if (episode != null) 1 else 0,
                milestone = episode
                    ?.takeIf { isWatch }
                    ?.let(TodayMilestone::fromEpisode),
            )
        }
    }
}
