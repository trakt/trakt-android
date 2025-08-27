package tv.trakt.trakt.core.home.sections.activity.usecases

import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter
import tv.trakt.trakt.core.profile.data.remote.UserRemoteDataSource

internal class GetActivityFilterUseCase(
    private val remoteSource: UserRemoteDataSource,
) {
    private var currentFilter = HomeActivityFilter.SOCIAL

    fun getFilter(): HomeActivityFilter {
        return currentFilter
    }

    fun setFilter(filter: HomeActivityFilter) {
        currentFilter = filter
    }
}
