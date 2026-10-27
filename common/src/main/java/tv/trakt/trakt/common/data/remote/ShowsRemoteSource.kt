package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.data.dto.AnticipatedShowDto
import tv.trakt.trakt.common.data.dto.TrendingShowDto
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.ParentalGuideCategory
import tv.trakt.trakt.common.networking.CalendarMediaDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.CastCrewDto
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.common.networking.ExternalShowRatingsDto
import tv.trakt.trakt.common.networking.ExtraVideoDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.SeasonDto
import tv.trakt.trakt.common.networking.ShowCalendarsDto
import tv.trakt.trakt.common.networking.ShowStatsDto
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendationsRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3SentimentResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3ShowRecommendationResponse
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

class ShowsRemoteSource(
    private val showsApi: ShowsApi,
    private val calendarsApi: CalendarsApi,
    private val v3Api: V3Api,
    private val recommendationsApi: RecommendationsApi,
) {
    suspend fun getTrending(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<TrendingShowDto> {
        val response = showsApi.getShowsTrending(
            extended = "full,streaming_ids,images,colors",
            page = page,
            limit = limit,
            theme = theme,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            parentalNudity = filters.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters.parentalParam(ParentalGuideCategory.Frightening),
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
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

    suspend fun getPopular(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<ShowCalendarsDto> {
        val response = showsApi.getShowsPopular(
            extended = "full,streaming_ids,images,colors",
            page = page,
            limit = limit,
            theme = theme,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            parentalNudity = filters.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters.parentalParam(ParentalGuideCategory.Frightening),
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
            ignoreWatched = filters.hideWatched,
            ignoreWatchlisted = filters.hideWatchlist,
            ignoreCollected = null,
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    suspend fun getRecommended(
        limit: Int,
        filters: GlobalFilter = GlobalFilter.Default,
    ): List<V3ShowRecommendationResponse> {
        return v3Api.getShowRecommendations(
            V3RecommendationsRequest(
                limit = limit,
                extended = "full,streaming_ids,images,colors",
                watchWindow = 25,
                watchnow = filters.availability?.joinToString(",") { it.slug },
                genres = filters.genre?.joinToString(",") { it.slug },
                subgenres = filters.subgenre?.joinToString(","),
                years = filters.years?.let { "${it.first}-${it.second}" },
                ratings = filters.rating?.let { "${it.first}-${it.second}" },
                runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
                certifications = filters.certification?.joinToString(",") { it.slug },
                parentalNudity = filters.parentalParam(ParentalGuideCategory.Nudity),
                parentalViolence = filters.parentalParam(ParentalGuideCategory.Violence),
                parentalProfanity = filters.parentalParam(ParentalGuideCategory.Profanity),
                parentalAlcohol = filters.parentalParam(ParentalGuideCategory.Alcohol),
                parentalFrightening = filters.parentalParam(ParentalGuideCategory.Frightening),
                countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
                statuses = filters.statuses?.joinToString(",") { it.slug },
                ignoreWatched = true,
                ignoreWatchlisted = filters.hideWatchlist,
                ignoreCollected = true,
            ),
        )
    }

    suspend fun getRecommendedV2(limit: Int): List<RecommendedShowDto> {
        val response = recommendationsApi.getRecommendationsShowsRecommend(
            extended = "full,streaming_ids,images,colors",
            limit = limit,
            watchWindow = 25,
            ignoreWatched = true,
            ignoreCollected = true,
            ignoreWatchlisted = true,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            runtimes = null,
            countries = null,
            certifications = null,
        )

        return response.body()
    }

    suspend fun getAnticipated(
        page: Int = 1,
        limit: Int,
        endDate: Instant? = null,
        filters: GlobalFilter = GlobalFilter.Default,
        theme: String? = null,
    ): List<AnticipatedShowDto> {
        val response = showsApi.getShowsAnticipated(
            extended = "full,streaming_ids,images,colors",
            page = page,
            limit = limit,
            theme = theme,
            watchnow = filters.availability?.joinToString(",") { it.slug },
            genres = filters.genre?.joinToString(",") { it.slug },
            subgenres = filters.subgenre?.joinToString(","),
            years = filters.years?.let { "${it.first}-${it.second}" },
            ratings = filters.rating?.let { "${it.first}-${it.second}" },
            runtimes = filters.runtime?.let { "${it.first}-${it.second}" },
            certifications = filters.certification?.joinToString(",") { it.slug },
            parentalNudity = filters.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters.parentalParam(ParentalGuideCategory.Frightening),
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
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

    suspend fun getReleases(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter = GlobalFilter.Default,
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
            parentalNudity = filters.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters.parentalParam(ParentalGuideCategory.Frightening),
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
            startDate2 = null,
            type = "show",
            group = null,
        )

        return response.body()
    }

    suspend fun getReleasesPremieres(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter = GlobalFilter.Default,
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
            parentalNudity = filters.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters.parentalParam(ParentalGuideCategory.Frightening),
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
            startDate2 = null,
        ).body()
    }

    suspend fun getReleasesFinales(
        startDate: Instant,
        days: Int,
        filters: GlobalFilter = GlobalFilter.Default,
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
            parentalNudity = filters.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters.parentalParam(ParentalGuideCategory.Frightening),
            countries = filters.countries?.joinToString(",") ?: filters.region?.slug,
            statuses = filters.statuses?.joinToString(",") { it.slug },
            startDate2 = null,
        ).body()
    }

    suspend fun getShowDetails(showId: TraktId): ShowCalendarsDto {
        return getShowSummary(showId.value.toString())
    }

    suspend fun getShowDetails(showSlug: SlugId): ShowCalendarsDto {
        return getShowSummary(showSlug.value)
    }

    private suspend fun getShowSummary(id: String): ShowCalendarsDto {
        val response = showsApi.getShowsSummary(
            id = id,
            extended = "full,streaming_ids,images,colors",
        )

        return response.body()
    }

    suspend fun getExternalRatings(showId: TraktId): ExternalShowRatingsDto {
        val response = showsApi.getShowsRatings(
            id = showId.value.toString(),
            extended = "all",
        )

        return response.body()
    }

    suspend fun getStudios(showId: TraktId): List<String> {
        val response = showsApi.getShowsStudios(
            id = showId.value.toString(),
        )

        return response.body().map { it.name }
    }

    suspend fun getStats(showId: TraktId): ShowStatsDto {
        val response = showsApi.getShowsStats(
            id = showId.value.toString(),
        )

        return response.body()
    }

    suspend fun getCastCrew(
        showId: TraktId,
        guestStars: Boolean = false,
        extended: String? = null,
    ): CastCrewDto {
        return try {
            showsApi.getShowsPeople(
                id = showId.value.toString(),
                extended = extended ?: if (guestStars) "images,full,guest_stars,markdown" else "images,full,markdown",
            ).body()
        } catch (error: Exception) {
            if (error.getHttpCode() == 204) {
                return CastCrewDto()
            }
            throw error
        }
    }

    suspend fun getSentiments(showId: TraktId): V3SentimentResponse? {
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

    suspend fun getRelated(
        showId: TraktId,
        limit: Int = 30,
    ): List<ShowCalendarsDto> {
        val response = showsApi.getShowsRelated(
            id = showId.value.toString(),
            extended = "full,streaming_ids,images,colors",
            limit = limit,
            page = null,
        )
        return response.body()
    }

    suspend fun getLists(
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

    suspend fun getComments(
        showId: TraktId,
        limit: Int,
        sort: String,
        language: String? = null,
        extended: String = "full,images,vip",
    ): List<CommentDto> {
        val response = showsApi.getShowsComments(
            id = showId.value.toString(),
            sort = sort,
            extended = extended,
            page = null,
            limit = limit.toString(),
            language = language,
        )

        return response.body()
    }

    suspend fun getExtras(showId: TraktId): List<ExtraVideoDto> {
        val response = showsApi.getShowsVideos(
            id = showId.value.toString(),
        )
        return response.body()
    }

    suspend fun getSeasons(showId: TraktId): List<SeasonDto> {
        val response = showsApi.getShowsSeasons(
            id = showId.value.toString(),
            extended = "full,images",
        )
        return response.body()
    }

    suspend fun postReport(
        showId: TraktId,
        reason: String,
        message: String,
    ) {
        showsApi.postShowsReport(
            id = showId.value.toString(),
            postMoviesReportRequest = createReportRequest(reason, message),
        )
    }
}
