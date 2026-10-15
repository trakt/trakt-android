package tv.trakt.trakt.core.today.data.local

import tv.trakt.trakt.core.today.model.FollowingActivity
import tv.trakt.trakt.core.today.model.TodayForYouItem

internal interface TodayLocalDataSource {
    suspend fun setActivities(activities: List<FollowingActivity>)

    suspend fun getActivities(): List<FollowingActivity>

    suspend fun setForYou(items: List<TodayForYouItem>)

    suspend fun getForYou(): List<TodayForYouItem>

    suspend fun setForYouWatched(
        key: String,
        watched: Boolean,
    )

    fun clear()
}
