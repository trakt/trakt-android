package tv.trakt.trakt.core.today.data.local

import tv.trakt.trakt.core.today.model.FollowingActivity

internal interface TodayLocalDataSource {
    suspend fun setActivities(activities: List<FollowingActivity>)

    suspend fun getActivities(): List<FollowingActivity>

    fun clear()
}
