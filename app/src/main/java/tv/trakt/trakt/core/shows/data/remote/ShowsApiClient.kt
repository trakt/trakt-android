package tv.trakt.trakt.core.shows.data.remote

import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.ShowDto
import tv.trakt.trakt.core.shows.data.remote.model.AnticipatedShowDto
import tv.trakt.trakt.core.shows.data.remote.model.TrendingShowDto
import java.time.Instant

internal class ShowsApiClient(
    private val showsApi: ShowsApi,
    private val recommendationsApi: RecommendationsApi,
) : ShowsRemoteDataSource {
    override suspend fun getTrending(limit: Int): List<TrendingShowDto> {
        val response = showsApi.getShowsTrending(
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
        val response = showsApi.getShowsHot(
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

    override suspend fun getPopular(
        limit: Int,
        years: Int,
    ): List<ShowDto> {
        val response = showsApi.getShowsPopular(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = years.toString(),
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

    override suspend fun getRecommended(limit: Int): List<RecommendedShowDto> {
        val response = recommendationsApi.getRecommendationsShowsRecommend(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            ignoreWatched = true,
            ignoreWatchlisted = true,
            ignoreCollected = true,
            watchWindow = 25,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getAnticipated(
        limit: Int,
        endDate: Instant,
    ): List<AnticipatedShowDto> {
        val response = showsApi.getShowsAnticipated(
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
            endDate = endDate.toString(),
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
