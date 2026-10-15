package tv.trakt.trakt.core.today.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.today.data.local.TodayLocalDataSource
import tv.trakt.trakt.core.today.data.remote.TodayRemoteDataSource
import tv.trakt.trakt.core.today.model.FollowingActivity
import tv.trakt.trakt.core.today.model.FollowingActivity.Target
import tv.trakt.trakt.core.today.model.TodayForYouItem
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.core.today.model.TodayStoryGroup
import tv.trakt.trakt.core.today.model.TodayTitleStory

internal class GetTodayStoriesUseCase(
    private val remoteSource: TodayRemoteDataSource,
    private val localSource: TodayLocalDataSource,
    private val getForYouUseCase: GetTodayForYouUseCase,
) {
    suspend fun getLocalStories(mode: MediaMode): ImmutableList<TodayStoryGroup> {
        return toStoryGroups(
            forYou = localSource.getForYou(),
            activities = localSource.getActivities(),
            mode = mode,
        )
    }

    suspend fun getStories(mode: MediaMode): ImmutableList<TodayStoryGroup> {
        return coroutineScope {
            val forYou = async { getForYou() }
            val activities = remoteSource.getFollowingActivities()

            localSource.setActivities(activities)
            localSource.setForYou(forYou.await())

            toStoryGroups(
                forYou = forYou.await(),
                activities = activities,
                mode = mode,
            )
        }
    }

    /** Refetches only the user's own releases, reusing the stored friend activity. */
    suspend fun refreshForYou(mode: MediaMode): ImmutableList<TodayStoryGroup> {
        val forYou = getForYouUseCase.getForYou()
        localSource.setForYou(forYou)

        return toStoryGroups(
            forYou = forYou,
            activities = localSource.getActivities(),
            mode = mode,
        )
    }

    private suspend fun getForYou(): List<TodayForYouItem> {
        return try {
            getForYouUseCase.getForYou()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            emptyList()
        }
    }

    private fun toStoryGroups(
        forYou: List<TodayForYouItem>,
        activities: List<FollowingActivity>,
        mode: MediaMode,
    ): ImmutableList<TodayStoryGroup> {
        return TodayStoryGroup.from(
            forYou = forYou.filter { it.media.isInMode(mode) },
            stories = TodayTitleStory.fromActivities(
                activities.filter { it.target.isInMode(mode) },
            ),
        )
    }
}

private fun Target.isInMode(mode: MediaMode): Boolean {
    return isInMode(isMovie = this is Target.MovieTarget, mode = mode)
}

private fun TodayMedia.isInMode(mode: MediaMode): Boolean {
    return isInMode(isMovie = this is TodayMedia.MovieMedia, mode = mode)
}

private fun isInMode(
    isMovie: Boolean,
    mode: MediaMode,
): Boolean {
    return when (mode) {
        MediaMode.Media -> true
        MediaMode.Movies -> isMovie
        MediaMode.Shows -> !isMovie
    }
}
