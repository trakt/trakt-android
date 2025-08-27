package tv.trakt.trakt.core.profile.data.remote

import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.networking.SocialActivityItemDto
import tv.trakt.trakt.common.networking.SyncHistoryEpisodeItemDto
import tv.trakt.trakt.common.networking.SyncHistoryMovieItemDto

internal interface UserRemoteDataSource {
    suspend fun getProfile(): User

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
}
