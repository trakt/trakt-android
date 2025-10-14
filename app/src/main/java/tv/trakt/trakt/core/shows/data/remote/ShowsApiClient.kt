package tv.trakt.trakt.core.shows.data.remote

import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.ExternalRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.ShowDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.core.shows.data.remote.model.AnticipatedShowDto
import tv.trakt.trakt.core.shows.data.remote.model.TrendingShowDto
import java.time.Instant

internal class ShowsApiClient(
    private val showsApi: ShowsApi,
    private val recommendationsApi: RecommendationsApi,
) : ShowsRemoteDataSource {
    override suspend fun getTrending(
        page: Int,
        limit: Int,
    ): List<TrendingShowDto> {
        val response = showsApi.getShowsTrending(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = page,
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
        page: Int,
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
            page = page,
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
        page: Int,
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
            page = page,
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

    override suspend fun getShowDetails(showId: TraktId): ShowDto {
        val response = showsApi.getShowsSummary(
            id = showId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
        )

        return response.body()
    }

    override suspend fun getExternalRatings(showId: TraktId): ExternalRatingsDto {
        val response = showsApi.getShowsRatings(
            id = showId.value.toString(),
            extended = "all",
        )

        return response.body()
    }

    override suspend fun getStudios(showId: TraktId): List<String> {
        val response = showsApi.getShowsStudios(
            id = showId.value.toString(),
        )

        return response.body().map { it.name }
    }

    override suspend fun getStreamings(
        showId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto> {
        val response = showsApi.getShowsWatchnow(
            country = countryCode ?: "",
            id = showId.value.toString(),
            links = "direct",
        )

        return response.body()
    }

    override suspend fun getCastCrew(showId: TraktId): CastCrewDto {
        val response = showsApi.getShowsPeople(
            id = showId.value.toString(),
            extended = "cloud9",
        )
        return response.body()
    }

    override suspend fun getSentiments(showId: TraktId): Sentiments {
        val response = showsApi.getShowsSentiments(
            id = showId.value.toString(),
        ).body()

        return Sentiments(
            good = response.good
                .map { Sentiments.Sentiment(it.sentiment) }
                .toImmutableList(),
            bad = response.bad
                .map { Sentiments.Sentiment(it.sentiment) }
                .toImmutableList(),
        )
    }

    override suspend fun getRelated(showId: TraktId): List<ShowDto> {
        val response = showsApi.getShowsRelated(
            id = showId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
            limit = 30,
            page = null,
        )
        return response.body()
    }

    override suspend fun getLists(
        showId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto> {
        val response = showsApi.getShowsLists(
            id = showId.value.toString(),
            type = type,
            extended = "images",
            page = null,
            limit = limit,
            sort = "popular",
        )

        return response.body()
    }

    override suspend fun getExtras(showId: TraktId): List<ExtraVideoDto> {
        val response = showsApi.getShowsVideos(
            id = showId.value.toString(),
        )
        return response.body()
    }
}
