package tv.trakt.trakt.core.shows.data.remote

import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.networking.ShowDto
import tv.trakt.trakt.core.shows.data.remote.model.AnticipatedShowDto
import tv.trakt.trakt.core.shows.data.remote.model.TrendingShowDto

internal class ShowsApiClient(
    private val api: ShowsApi,
) : ShowsRemoteDataSource {
    override suspend fun getTrending(limit: Int): List<TrendingShowDto> {
        val response = api.getShowsTrending(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
            .map {
                TrendingShowDto(
                    watchers = it.watchers,
                    show = it.show,
                )
            }
    }

    override suspend fun getHot(limit: Int): List<TrendingShowDto> {
        val response = api.getShowsHot(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = "lastmonth",
            endDate = null,
        )

        return response.body()
            .map {
                TrendingShowDto(
                    watchers = it.listCount,
                    show = it.show,
                )
            }
    }

    override suspend fun getPopular(limit: Int): List<ShowDto> {
        val response = api.getShowsPopular(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getAnticipated(limit: Int): List<AnticipatedShowDto> {
        val response = api.getShowsAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = null,
            ignoreWatched = null,
            ignoreCollected = null,
            ignoreWatchlisted = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
            .map {
                AnticipatedShowDto(
                    listCount = it.listCount,
                    show = it.show,
                )
            }
    }
}
