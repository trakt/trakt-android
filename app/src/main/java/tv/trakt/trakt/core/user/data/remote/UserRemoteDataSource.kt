package tv.trakt.trakt.core.user.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import tv.trakt.trakt.common.networking.WatchedMovieDto
import tv.trakt.trakt.common.networking.WatchedShowDto
import tv.trakt.trakt.common.networking.WatchlistItemDto
import java.time.LocalDate

internal interface UserRemoteDataSource {
    suspend fun getProfile(): User

    suspend fun getWatchedMovies(): List<WatchedMovieDto>

//    suspend fun getProgressShows(limit: Int?): List<SyncProgressItemDto>

    suspend fun getWatchedShows(): List<WatchedShowDto>

    suspend fun getWatchlist(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sort: String? = null,
    ): List<WatchlistItemDto>

    suspend fun getSocialActivity(
        page: Int? = null,
        limit: Int,
        type: String,
    ): List<SocialActivityItemDto>

    suspend fun getEpisodesHistory(
        page: Int = 1,
        limit: Int,
    ): List<SyncHistoryEpisodeItemDto>

    suspend fun getMoviesHistory(
        page: Int = 1,
        limit: Int,
    ): List<SyncHistoryMovieItemDto>

    suspend fun getMovieHistory(
        movieId: TraktId,
        page: Int = 1,
        limit: Int,
    ): List<SyncHistoryMovieItemDto>

    suspend fun getShowHistory(
        showId: TraktId,
        page: Int = 1,
        limit: Int? = null,
    ): List<SyncHistoryEpisodeItemDto>

    suspend fun getShowsCalendar(
        startDate: LocalDate,
        days: Int,
    ): List<CalendarShowDto>

    suspend fun getMoviesCalendar(
        startDate: LocalDate,
        days: Int,
    ): List<CalendarMovieDto>

    suspend fun getPersonalLists(): List<ListDto>

    suspend fun getPersonalListItems(
        listId: TraktId,
        limit: Int?,
        page: Int = 1,
        extended: String,
    ): List<ListItemDto>
}
