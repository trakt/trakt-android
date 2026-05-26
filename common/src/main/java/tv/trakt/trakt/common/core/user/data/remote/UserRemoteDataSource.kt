package tv.trakt.trakt.common.core.user.data.remote

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.DroppedItemDto
import tv.trakt.trakt.common.networking.SyncLibraryMediaDto
import tv.trakt.trakt.common.networking.UserWatchingDto

interface UserRemoteDataSource {
    suspend fun getWatchingNow(): UserWatchingDto?

    suspend fun getWatchedMovies(pagination: Pagination): Map<String, List<String>>

    suspend fun getWatchedShows(pagination: Pagination): Map<String, Map<String, Map<String, List<String>>>>

    suspend fun getLibrary(
        extended: String? = null,
        availableOn: String? = null,
        page: Int,
        limit: Int,
    ): List<SyncLibraryMediaDto>

    suspend fun getDroppedShows(
        page: Int,
        limit: Int,
    ): List<DroppedItemDto>

    // Profile & Settings

    suspend fun getProfile(): User

    suspend fun getUserProfile(userId: TraktId): User

    suspend fun updateProfileLocation(location: String?)

    suspend fun updateProfileDisplayName(displayName: String?)

    suspend fun updateProfileAbout(about: String?)

    suspend fun updateMultiplePlays(enabled: Boolean)

    suspend fun updateCoverImage(
        mediaId: TraktId?,
        mediaType: MediaType?,
    )
}
