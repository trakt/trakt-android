package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.User
import java.time.Instant

/** All friend actions on a single title within the Today window. */
@Immutable
internal data class TodayTitleStory(
    val media: TodayMedia,
    val actions: ImmutableList<TodayFriendAction>,
    val users: ImmutableList<User>,
    val averageRating: Float?,
    val milestone: TodayMilestone?,
    val latestAt: Instant,
) {
    val key: String
        get() = media.key

    companion object {
        /** Groups activities by title, newest title first. */
        fun fromActivities(activities: List<FollowingActivity>): List<TodayTitleStory> {
            return activities
                .distinctBy { it.key }
                .groupBy { it.target.media.key }
                .values
                .map { group ->
                    fromActions(
                        media = group.first().target.media,
                        actions = group.map(TodayFriendAction::fromActivity),
                    )
                }
                .sortedByDescending { it.latestAt }
        }

        private fun fromActions(
            media: TodayMedia,
            actions: List<TodayFriendAction>,
        ): TodayTitleStory {
            val merged = mergeEpisodeWatches(actions)

            return TodayTitleStory(
                media = media,
                actions = merged.toImmutableList(),
                users = merged
                    .map { it.user }
                    .distinctBy { it.ids.trakt }
                    .toImmutableList(),
                averageRating = averageRating(merged),
                milestone = TodayMilestone.strongest(merged.map { it.milestone }),
                latestAt = merged.firstOrNull()?.activityAt ?: Instant.EPOCH,
            )
        }

        /**
         * Collapses each friend's episode watches into their most recent one, so a binge
         * reads as one action instead of one per episode.
         */
        private fun mergeEpisodeWatches(actions: List<TodayFriendAction>): List<TodayFriendAction> {
            return actions
                .sortedByDescending { it.activityAt }
                .fold(LinkedHashMap<String, TodayFriendAction>()) { result, action ->
                    if (!action.isEpisodeWatch) {
                        result[action.key] = action
                        return@fold result
                    }

                    val key = "watch-${action.user.ids.trakt.value}"
                    val latest = result[key]
                    result[key] = latest?.copy(
                        episodeCount = latest.episodeCount + action.episodeCount,
                        milestone = TodayMilestone.strongest(listOf(latest.milestone, action.milestone)),
                    ) ?: action
                    result
                }
                .values
                .sortedByDescending { it.activityAt }
        }

        private fun averageRating(actions: List<TodayFriendAction>): Float? {
            val ratings = actions
                .filter { it.isTitleRating }
                .distinctBy { it.user.ids.trakt }
                .mapNotNull { it.rating }

            if (ratings.isEmpty()) return null
            return ratings.average().toFloat()
        }
    }
}
