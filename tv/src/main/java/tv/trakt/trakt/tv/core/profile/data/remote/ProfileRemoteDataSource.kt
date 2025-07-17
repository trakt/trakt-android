package tv.trakt.trakt.tv.core.profile.data.remote

import tv.trakt.trakt.tv.common.model.User
import tv.trakt.trakt.tv.networking.openapi.CalendarShowDto
import tv.trakt.trakt.tv.networking.openapi.SyncFavoriteMovieDto
import tv.trakt.trakt.tv.networking.openapi.SyncFavoriteShowDto
import tv.trakt.trakt.tv.networking.openapi.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.tv.networking.openapi.SyncHistoryMovieItemDto
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
