package tv.trakt.trakt.common.core.today.data.remote

import tv.trakt.trakt.common.core.today.model.FollowingActivity
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.api.v3.V3Api

private const val PAGE_LIMIT = 250

// Caps a runaway feed; a day of activity from followed users fits well within it.
private const val MAX_PAGES = 4

class TodayRemoteSource(
    private val v3Api: V3Api,
) {
    /** All activity of followed users within the API's rolling Today window. */
    suspend fun getFollowingActivities(): List<FollowingActivity> {
        val entries = buildList {
            for (page in 1..MAX_PAGES) {
                val items = v3Api.getFollowingActivities(
                    pagination = Pagination(page = page, limit = PAGE_LIMIT),
                )
                addAll(items)
                if (items.size < PAGE_LIMIT) break
            }
        }

        return entries.mapNotNull(FollowingActivity::fromDto)
    }
}
