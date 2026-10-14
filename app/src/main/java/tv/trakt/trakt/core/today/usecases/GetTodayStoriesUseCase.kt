package tv.trakt.trakt.core.today.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.today.data.local.TodayLocalDataSource
import tv.trakt.trakt.core.today.data.remote.TodayRemoteDataSource
import tv.trakt.trakt.core.today.model.FollowingActivity
import tv.trakt.trakt.core.today.model.FollowingActivity.Target
import tv.trakt.trakt.core.today.model.TodayStoryGroup
import tv.trakt.trakt.core.today.model.TodayTitleStory

internal class GetTodayStoriesUseCase(
    private val remoteSource: TodayRemoteDataSource,
    private val localSource: TodayLocalDataSource,
) {
    suspend fun getLocalStories(mode: MediaMode): ImmutableList<TodayStoryGroup> {
        return toStoryGroups(
            activities = localSource.getActivities(),
            mode = mode,
        )
    }

    suspend fun getStories(mode: MediaMode): ImmutableList<TodayStoryGroup> {
        val activities = remoteSource.getFollowingActivities()
        localSource.setActivities(activities)

        return toStoryGroups(
            activities = activities,
            mode = mode,
        )
    }

    private fun toStoryGroups(
        activities: List<FollowingActivity>,
        mode: MediaMode,
    ): ImmutableList<TodayStoryGroup> {
        val filtered = activities.filter { it.isInMode(mode) }
        return TodayTitleStory.fromActivities(filtered)
            .map(TodayStoryGroup::fromStory)
            .toImmutableList()
    }
}

private fun FollowingActivity.isInMode(mode: MediaMode): Boolean {
    val isMovie = target is Target.MovieTarget
    return when (mode) {
        MediaMode.Media -> true
        MediaMode.Movies -> isMovie
        MediaMode.Shows -> !isMovie
    }
}
