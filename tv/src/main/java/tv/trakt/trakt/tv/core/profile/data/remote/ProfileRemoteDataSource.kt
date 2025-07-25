package tv.trakt.trakt.tv.core.profile.data.remote

import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import tv.trakt.trakt.tv.common.model.User
import java.time.LocalDate

internal interface ProfileRemoteDataSource {
    suspend fun getUserProfile(): User

    suspend fun getUserShowsCalendar(
        startDate: LocalDate,
        days: Int,
    ): List<CalendarShowDto>

    suspend fun getUserEpisodesHistory(
        page: Int = 1,
        limit: Int,
    ): List<SyncHistoryEpisodeItemDto>

    suspend fun getUserMoviesHistory(
        page: Int = 1,
        limit: Int,
    ): List<SyncHistoryMovieItemDto>

    suspend fun getUserFavoriteShows(
        page: Int = 1,
        limit: Int,
    ): List<SyncFavoriteShowDto>

    suspend fun getUserFavoriteMovies(
        page: Int = 1,
        limit: Int,
    ): List<SyncFavoriteMovieDto>
}
