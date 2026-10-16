package tv.trakt.trakt.common.core.today.data.remote

import tv.trakt.trakt.common.core.today.model.FollowingActivity

interface TodayRemoteDataSource {
    /** All activity of followed users within the API's rolling Today window. */
    suspend fun getFollowingActivities(): List<FollowingActivity>
}
