package tv.trakt.trakt.app.core.shows.data.remote

import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.app.core.shows.data.remote.model.response.AnticipatedShowDto
import tv.trakt.trakt.app.core.shows.data.remote.model.response.TrendingShowDto
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.SeasonDto
import tv.trakt.trakt.common.networking.ShowDto
import tv.trakt.trakt.common.networking.StreamingDto

internal class ShowsApiClient(
    private val api: ShowsApi,
    private val recommendationsApi: RecommendationsApi,
) : ShowsRemoteDataSource {
    override suspend fun getTrendingShows(): List<TrendingShowDto> {
        val response = api.getShowsTrending(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
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

    override suspend fun getMonthlyHotShows(): List<TrendingShowDto> {
        val response = api.getShowsHot(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
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

    override suspend fun getPopularShows(): List<ShowDto> {
        val response = api.getShowsPopular(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
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

    override suspend fun getAnticipatedShows(): List<AnticipatedShowDto> {
        val response = api.getShowsAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
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

    override suspend fun getRecommendedShows(): List<RecommendedShowDto> {
        val response = recommendationsApi.getRecommendationsShowsRecommend(
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
            watchWindow = 25,
            ignoreWatched = true,
            ignoreCollected = true,
            ignoreWatchlisted = true,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getRelatedShows(showId: TraktId): List<ShowDto> {
        val response = api.getShowsRelated(
            id = showId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
            limit = 20,
            page = null,
        )

        return response.body()
    }

    override suspend fun getShowExternalRatings(showId: TraktId): ExternalRatingsDto {
        val response = api.getShowsRatings(
            id = showId.value.toString(),
            extended = "all",
        )

        return response.body()
    }

    override suspend fun getShowExtras(showId: TraktId): List<ExtraVideoDto> {
        val response = api.getShowsVideos(
            id = showId.value.toString(),
        )
        return response.body()
    }

    override suspend fun getShowCastCrew(showId: TraktId): CastCrewDto {
        val response = api.getShowsPeople(
            id = showId.value.toString(),
            extended = "cloud9",
        )
        return response.body()
    }

    override suspend fun getShowComments(showId: TraktId): List<CommentDto> {
        val response = api.getShowsComments(
            id = showId.value.toString(),
            sort = "likes",
            extended = "images",
            page = null,
            limit = 20.toString(),
        )

        return response.body()
    }

    override suspend fun getShowLists(
        showId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto> {
        val response = api.getShowsLists(
            id = showId.value.toString(),
            type = type,
            extended = "images",
            page = null,
            limit = limit,
            sort = "popular",
        )

        return response.body()
    }

    override suspend fun getShowDetails(showId: TraktId): ShowDto {
        val response = api.getShowsSummary(
            id = showId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
        )

        return response.body()
    }

    override suspend fun getShowStreamings(
        showId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto> {
        val response = api.getShowsWatchnow(
            country = countryCode ?: "",
            id = showId.value.toString(),
            links = "direct",
        )

        return response.body()
    }

    override suspend fun getShowSeasons(showId: TraktId): List<SeasonDto> {
        val response = api.getShowsSeasons(
            id = showId.value.toString(),
            extended = "full,cloud9",
        )

        return response.body()
    }
}
