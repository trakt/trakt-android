package tv.trakt.trakt.core.shows.data.remote

import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.CalendarMediaDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.ExternalShowRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.SeasonDto
import tv.trakt.trakt.common.networking.ShowCalendarsDto
import tv.trakt.trakt.common.networking.ShowStatsDto
import tv.trakt.trakt.common.networking.StreamingDto
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import tv.trakt.trakt.core.shows.data.remote.model.AnticipatedShowDto
import tv.trakt.trakt.core.shows.data.remote.model.TrendingShowDto
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

internal class ShowsApiClient(
    private val showsApi: ShowsApi,
    private val recommendationsApi: RecommendationsApi,
    private val calendarsApi: CalendarsApi,
    private val v3Api: V3Api,
) : ShowsRemoteDataSource {
    override suspend fun getTrending(
        page: Int,
        limit: Int,
        filters: GlobalFilter,
    ): List<TrendingShowDto> {
        val response = showsApi.getShowsTrending(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
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

    override suspend fun getPopular(
        page: Int,
        limit: Int,
        filters: GlobalFilter,
    ): List<ShowCalendarsDto> {
        val response = showsApi.getShowsPopular(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter,
    ): List<RecommendedShowDto> {
        val response = recommendationsApi.getRecommendationsShowsRecommend(
            extended = "full,streaming_ids,cloud9,colors",
            limit = limit,
            watchWindow = 25,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            ignoreWatched = true,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = true,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getAnticipated(
        page: Int,
        limit: Int,
        endDate: Instant?,
        filters: GlobalFilter,
    ): List<AnticipatedShowDto> {
        val response = showsApi.getShowsAnticipated(
            extended = "full,streaming_ids,cloud9,colors",
            page = page,
            limit = limit,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = endDate?.toString(),
        )

        return response.body()
            .map {
                AnticipatedShowDto(
                    listCount = it.listCount,
                    show = it.show,
                )
            }
    }

    override suspend fun getReleases(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter,
    ): List<CalendarMediaDto> {
        val response = calendarsApi.getCalendarsReleasesHot(
            extended = "full,images,colors",
            startDate = startDate.truncatedTo(DAYS).toString(),
            endDate = null,
            days = days,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            startDate2 = null,
            type = "show",
            group = null,
        )

        return response.body()
    }

    override suspend fun getReleasesPremieres(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter,
    ): List<CalendarShowDto> {
        return calendarsApi.getCalendarsReleasesHotPremieres(
            extended = "full,images,colors",
            startDate = startDate.truncatedTo(DAYS).toString(),
            endDate = null,
            days = days,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            startDate2 = null,
        ).body()
    }

    override suspend fun getReleasesFinales(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter,
    ): List<CalendarShowDto> {
        return calendarsApi.getCalendarsReleasesHotFinales(
            extended = "full,images,colors",
            startDate = startDate.truncatedTo(DAYS).toString(),
            endDate = null,
            days = days,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            startDate2 = null,
        ).body()
    }

    override suspend fun getShowDetails(showId: TraktId): ShowCalendarsDto {
        val response = showsApi.getShowsSummary(
            id = showId.value.toString(),
            extended = "full,streaming_ids,cloud9,colors",
        )

        return response.body()
    }

    override suspend fun getExternalRatings(showId: TraktId): ExternalShowRatingsDto {
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

    override suspend fun getStats(showId: TraktId): ShowStatsDto {
        val response = showsApi.getShowsStats(
            id = showId.value.toString(),
        )

        return response.body()
    }

    override suspend fun getStreamings(
        showId: TraktId,
        countryCode: String?,
    ): Map<String, StreamingDto> {
        val response = showsApi.getShowsWatchnow(
            country = countryCode ?: "",
            id = showId.value.toString(),
            links = "direct",
            extended = "streaming_ranks",
        )

        return response.body()
    }

    override suspend fun getJustWatchLink(
        showId: TraktId,
        countryCode: String?,
    ): String? {
        val response = showsApi.getShowsJustwatchLink(
            country = countryCode ?: "",
            id = showId.value.toString(),
        )
        val body = response.body()
        return body[countryCode]
    }

    override suspend fun getCastCrew(showId: TraktId): CastCrewDto {
        return try {
            showsApi.getShowsPeople(
                id = showId.value.toString(),
                extended = "cloud9,full",
            ).body()
        } catch (error: Exception) {
            if (error.getHttpCode() == 204) {
                return CastCrewDto()
            }
            throw error
        }
    }

    override suspend fun getSentiments(showId: TraktId): V3SentimentResponse? {
        return try {
            v3Api.getShowSentiment(showId)
        } catch (error: Exception) {
            // 404 means no sentiment data is available for this show, so we return null.
            if (error.getHttpCode() == 404) {
                return null
            }
            throw error
        }
    }

    override suspend fun getRelated(showId: TraktId): List<ShowCalendarsDto> {
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

    override suspend fun getComments(
        showId: TraktId,
        limit: Int,
        sort: String,
    ): List<tv.trakt.trakt.common.networking.CommentDto> {
        val response = showsApi.getShowsComments(
            id = showId.value.toString(),
            sort = sort,
            extended = "full,images,vip",
            page = null,
            limit = limit.toString(),
            language = null,
        )

        return response.body()
    }

    override suspend fun getExtras(showId: TraktId): List<ExtraVideoDto> {
        val response = showsApi.getShowsVideos(
            id = showId.value.toString(),
        )
        return response.body()
    }

    override suspend fun getSeasons(showId: TraktId): List<SeasonDto> {
        val response = showsApi.getShowsSeasons(
            id = showId.value.toString(),
            extended = "full,cloud9",
        )
        return response.body()
    }
}
