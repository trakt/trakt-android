package tv.trakt.trakt.tv.core.shows.data.remote

import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.shows.data.remote.model.response.AnticipatedShowDto
import tv.trakt.trakt.tv.core.shows.data.remote.model.response.TrendingShowDto
import tv.trakt.trakt.tv.networking.openapi.CastCrewDto
import tv.trakt.trakt.tv.networking.openapi.CommentDto
import tv.trakt.trakt.tv.networking.openapi.ExternalRatingsDto
import tv.trakt.trakt.tv.networking.openapi.ExtraVideoDto
import tv.trakt.trakt.tv.networking.openapi.ListDto
import tv.trakt.trakt.tv.networking.openapi.RecommendedShowDto
import tv.trakt.trakt.tv.networking.openapi.SeasonDto
import tv.trakt.trakt.tv.networking.openapi.ShowDto
import tv.trakt.trakt.tv.networking.openapi.StreamingDto

internal interface ShowsRemoteDataSource {
    suspend fun getTrendingShows(): List<TrendingShowDto>

    suspend fun getMonthlyHotShows(): List<TrendingShowDto>

    suspend fun getPopularShows(): List<ShowDto>

    suspend fun getAnticipatedShows(): List<AnticipatedShowDto>

    suspend fun getRecommendedShows(): List<RecommendedShowDto>

    suspend fun getRelatedShows(showId: TraktId): List<ShowDto>

    suspend fun getShowDetails(showId: TraktId): ShowDto?

    suspend fun getShowExternalRatings(showId: TraktId): ExternalRatingsDto

    suspend fun getShowExtras(showId: TraktId): List<ExtraVideoDto>

    suspend fun getShowCastCrew(showId: TraktId): CastCrewDto

    suspend fun getShowComments(showId: TraktId): List<CommentDto>

    suspend fun getShowLists(
        showId: TraktId,
        type: String,
        limit: Int,
    ): List<ListDto>

    suspend fun getShowStreamings(
        showId: TraktId,
        countryCode: String,
    ): Map<String, StreamingDto>

    suspend fun getShowSeasons(showId: TraktId): List<SeasonDto>
}
