package tv.trakt.trakt.core.user.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.CalendarMovieDto
import tv.trakt.trakt.common.networking.CalendarShowDto
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.SyncFavoriteMovieDto
import tv.trakt.trakt.common.networking.SyncFavoriteShowDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto
import tv.trakt.trakt.common.networking.UserCommentsDto
import tv.trakt.trakt.common.networking.UserRatingDto
import tv.trakt.trakt.common.networking.WatchedShowDto
import tv.trakt.trakt.common.networking.WatchlistItemDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import java.time.LocalDate
import java.time.ZonedDateTime

internal interface UserRemoteDataSource {
    suspend fun getProfile(): User

    suspend fun updateProfileLocation(location: String?)

    suspend fun updateProfileDisplayName(displayName: String?)

    suspend fun updateProfileAbout(about: String?)

    suspend fun getWatchedMovies(): Map<String, List<String>>

    suspend fun getWatchedShows(): List<WatchedShowDto>

    suspend fun getRatingsShows(): List<UserRatingDto>

    suspend fun getRatingsMovies(): List<UserRatingDto>

    suspend fun getRatingsEpisodes(): List<UserRatingDto>

    suspend fun getFavoriteShows(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sort: String? = null,
    ): List<SyncFavoriteShowDto>

    suspend fun getFavoriteMovies(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sort: String? = null,
    ): List<SyncFavoriteMovieDto>

    suspend fun getWatchlist(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sort: String? = null,
    ): List<WatchlistItemDto>

    suspend fun getWatchlistMovies(
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
        sort: String? = null,
        hide: String? = null,
    ): List<WatchlistMovieDto>

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

    suspend fun getEpisodeHistory(
        episodeId: TraktId,
        page: Int = 1,
        limit: Int?,
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
        sorting: Sorting,
    ): List<ListItemDto>

    suspend fun getPersonalListShowItems(
        listId: TraktId,
        limit: Int?,
        page: Int = 1,
        extended: String,
        sorting: Sorting,
    ): List<ListShowItemDto>

    suspend fun getPersonalListMovieItems(
        listId: TraktId,
        limit: Int?,
        page: Int = 1,
        extended: String,
        sorting: Sorting,
    ): List<ListMovieItemDto>

    suspend fun getFollowing(): Map<UserCommentsDto, ZonedDateTime>

    suspend fun getFollowers(): Map<UserCommentsDto, ZonedDateTime>
}
