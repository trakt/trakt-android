package tv.trakt.trakt.tv.core.shows.data.remote

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
import tv.trakt.trakt.tv.core.shows.data.remote.model.response.AnticipatedShowDto
import tv.trakt.trakt.tv.core.shows.data.remote.model.response.TrendingShowDto

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
