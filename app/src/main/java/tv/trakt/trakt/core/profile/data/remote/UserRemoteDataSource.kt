package tv.trakt.trakt.core.profile.data.remote

import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import tv.trakt.trakt.common.networking.WatchlistItemDto
import java.time.LocalDate

internal interface UserRemoteDataSource {
    suspend fun getProfile(): User

    suspend fun getWatchlist(
        page: Int = 1,
        limit: Int,
        sort: String = "rank",
        extended: String,
    ): List<WatchlistItemDto>

    suspend fun getSocialActivity(
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

    suspend fun getShowsCalendar(
        startDate: LocalDate,
        days: Int,
    ): List<CalendarShowDto>

    suspend fun getMoviesCalendar(
        startDate: LocalDate,
        days: Int,
    ): List<CalendarMovieDto>

    suspend fun getPersonalLists(): List<ListDto>
}
