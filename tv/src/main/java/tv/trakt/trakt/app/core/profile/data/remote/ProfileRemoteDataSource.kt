package tv.trakt.trakt.app.core.profile.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
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

    suspend fun getUserLists(): List<ListDto>

    suspend fun getUserShowListItems(
        listId: TraktId,
        limit: Int,
        page: Int = 1,
        extended: String,
    ): List<ListShowItemDto>

    suspend fun getUserMovieListItems(
        listId: TraktId,
        limit: Int,
        page: Int = 1,
        extended: String,
    ): List<ListMovieItemDto>

    suspend fun getUserSocialActivity(
        limit: Int,
        type: String,
    ): List<SocialActivityItemDto>
}
