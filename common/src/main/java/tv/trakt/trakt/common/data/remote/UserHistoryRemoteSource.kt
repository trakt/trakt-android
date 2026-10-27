package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.HistoryApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.ParentalGuideCategory
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import kotlin.time.Instant

class UserHistoryRemoteSource(
    private val historyApi: HistoryApi,
) {
    suspend fun getMediaHistory(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter?,
        from: Instant? = null,
        to: Instant? = null,
        userId: String = "me",
        extended: String = "full,images,colors",
    ): List<SyncHistoryItemDto> {
        val response = historyApi.getUsersHistoryAll(
            id = userId,
            extended = extended,
            startAt = from?.toString(),
            endAt = to?.toString(),
            page = page,
            limit = limit,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            parentalNudity = filters?.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters?.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters?.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters?.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters?.parentalParam(ParentalGuideCategory.Frightening),
        )
        return response.body()
    }

    suspend fun getEpisodesHistory(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter?,
        from: Instant? = null,
        to: Instant? = null,
        userId: String = "me",
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryEpisodes(
            id = userId,
            extended = "full,images,colors",
            startAt = from?.toString(),
            endAt = to?.toString(),
            page = page,
            limit = limit,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            parentalNudity = filters?.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters?.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters?.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters?.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters?.parentalParam(ParentalGuideCategory.Frightening),
        )
        return response.body()
    }

    suspend fun getMoviesHistory(
        page: Int = 1,
        limit: Int,
        filters: GlobalFilter?,
        from: Instant? = null,
        to: Instant? = null,
        userId: String = "me",
    ): List<SyncHistoryMovieItemDto> {
        val response = historyApi.getUsersHistoryMovies(
            id = userId,
            extended = "full,images,colors",
            startAt = from?.toString(),
            endAt = to?.toString(),
            page = page,
            limit = limit,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            statuses = filters?.statuses?.joinToString(",") { it.slug },
            certifications = filters?.certification?.joinToString(",") { it.slug },
            parentalNudity = filters?.parentalParam(ParentalGuideCategory.Nudity),
            parentalViolence = filters?.parentalParam(ParentalGuideCategory.Violence),
            parentalProfanity = filters?.parentalParam(ParentalGuideCategory.Profanity),
            parentalAlcohol = filters?.parentalParam(ParentalGuideCategory.Alcohol),
            parentalFrightening = filters?.parentalParam(ParentalGuideCategory.Frightening),
        )
        return response.body()
    }

    suspend fun getMovieHistory(
        movieId: TraktId,
        page: Int = 1,
        limit: Int,
    ): List<SyncHistoryMovieItemDto> {
        val response = historyApi.getUsersHistoryMovie(
            id = "me",
            itemId = movieId.value.toString(),
            extended = "full,images,colors",
            startAt = null,
            endAt = null,
            page = page,
            limit = limit,
        )
        return response.body()
    }

    suspend fun getShowHistory(
        showId: TraktId,
        pagination: Pagination,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryShow(
            id = "me",
            itemId = showId.value.toString(),
            extended = "full,images,colors",
            startAt = null,
            endAt = null,
            page = pagination.page,
            limit = pagination.limit,
        )
        return response.body()
    }

    suspend fun getEpisodeHistory(
        episodeId: TraktId,
        page: Int = 1,
        limit: Int?,
    ): List<SyncHistoryEpisodeItemDto> {
        val response = historyApi.getUsersHistoryEpisode(
            id = "me",
            itemId = episodeId.value.toString(),
            extended = "full,images,colors",
            startAt = null,
            endAt = null,
            page = page,
            limit = when {
                limit == null -> 99_999
                else -> limit
            },
        )
        return response.body()
    }
}
