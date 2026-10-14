package tv.trakt.trakt.core.today.data.remote

import tv.trakt.trakt.core.today.model.FollowingActivity

internal interface TodayRemoteDataSource {
    /** All activity of followed users within the API's rolling Today window. */
    suspend fun getFollowingActivities(): List<FollowingActivity>
}
