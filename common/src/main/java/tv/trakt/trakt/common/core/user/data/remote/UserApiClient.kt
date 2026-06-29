package tv.trakt.trakt.common.core.user.data.remote

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PutUsersCoverRequest
import org.openapitools.client.models.PutUsersSaveSettingsRequest
import org.openapitools.client.models.PutUsersSaveSettingsRequestBrowsing
import org.openapitools.client.models.PutUsersSaveSettingsRequestUser
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.CommentAllDto
import tv.trakt.trakt.common.networking.DroppedItemDto
import tv.trakt.trakt.common.networking.SyncLibraryMediaDto
import tv.trakt.trakt.common.networking.UserWatchingDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class UserApiClient(
    private val usersApi: UsersApi,
    private val syncApi: SyncApi,
    private val cacheMarkerProvider: CacheMarkerProvider,
) : UserRemoteDataSource {
    override suspend fun getProfile(): User {
        val response = usersApi.getUsersSettings(
            extended = "browsing",
        ).body()

        return User.fromDto(response)
    }

    override suspend fun getUserProfile(userId: TraktId): User {
        val response = usersApi.getUsersProfile(
            id = userId.value.toString(),
            extended = "full,vip,images",
        ).body()

        return User.fromDto(response)
    }

    override suspend fun getWatchingNow(): UserWatchingDto? {
        val response = usersApi.getUsersWatching(
            id = "me",
            extended = "full,cloud9,colors",
        )
        return when {
            response.status == 204 -> null
            else -> response.body()
        }
    }

    override suspend fun updateProfileLocation(location: String?) {
        usersApi.putUsersSaveSettings(
            putUsersSaveSettingsRequest = PutUsersSaveSettingsRequest(
                user = PutUsersSaveSettingsRequestUser(
                    location = location,
                ),
            ),
        )

        cacheMarkerProvider.invalidate()
    }

    override suspend fun updateProfileDisplayName(displayName: String?) {
        usersApi.putUsersSaveSettings(
            putUsersSaveSettingsRequest = PutUsersSaveSettingsRequest(
                user = PutUsersSaveSettingsRequestUser(
                    name = displayName,
                ),
            ),
        )

        cacheMarkerProvider.invalidate()
    }

    override suspend fun updateProfileAbout(about: String?) {
        usersApi.putUsersSaveSettings(
            putUsersSaveSettingsRequest = PutUsersSaveSettingsRequest(
                user = PutUsersSaveSettingsRequestUser(
                    about = about,
                ),
            ),
        )

        cacheMarkerProvider.invalidate()
    }

    override suspend fun updateMultiplePlays(enabled: Boolean) {
        usersApi.putUsersSaveSettings(
            putUsersSaveSettingsRequest = PutUsersSaveSettingsRequest(
                browsing = PutUsersSaveSettingsRequestBrowsing(
                    watchOnlyOnce = !enabled,
                ),
            ),
        )

        cacheMarkerProvider.invalidate()
    }

    override suspend fun updateCoverImage(
        mediaId: TraktId?,
        mediaType: MediaType?,
    ) {
        if (mediaId == null || mediaType == null) {
            // Clear the custom cover image
            usersApi.putUsersCover(
                putUsersCoverRequest = PutUsersCoverRequest(
                    coverId = 0,
                    coverType = PutUsersCoverRequest.CoverType.SHOW,
                ),
            )
            cacheMarkerProvider.invalidate()
            return
        }

        usersApi.putUsersCover(
            putUsersCoverRequest = PutUsersCoverRequest(
                coverId = mediaId.value,
                coverType = when (mediaType) {
                    MediaType.Show -> PutUsersCoverRequest.CoverType.SHOW
                    MediaType.Movie -> PutUsersCoverRequest.CoverType.MOVIE
                    MediaType.Episode -> PutUsersCoverRequest.CoverType.EPISODE
                    else -> throw IllegalStateException("Unsupported media type for cover image: $mediaType")
                },
            ),
        )

        cacheMarkerProvider.invalidate()
    }

    override suspend fun getWatchedMovies(pagination: Pagination): Map<String, List<String>> {
        val response = usersApi.getUsersWatchedMinimalMovies(
            id = "me",
            extended = "min",
            page = pagination.page,
            limit = pagination.limit,
        )

        return response.body()
    }

    override suspend fun getWatchedShows(pagination: Pagination): Map<String, Map<String, Map<String, List<String>>>> {
        val response = usersApi.getUsersWatchedMinimalShows(
            id = "me",
            extended = "min",
            specials = true,
            seasonNumbers = true,
            page = pagination.page,
            limit = pagination.limit,
        )

        return response.body()
    }

    override suspend fun getLibrary(
        extended: String?,
        availableOn: String?,
        page: Int,
        limit: Int,
    ): List<SyncLibraryMediaDto> {
        val response = syncApi.getSyncCollectionMedia(
            extended = extended ?: "",
            availableOn = availableOn,
            page = page,
            limit = limit,
        )
        return response.body()
    }

    override suspend fun getDroppedShows(
        page: Int,
        limit: Int,
    ): List<DroppedItemDto> {
        val response = usersApi.getUsersHiddenDropped(
            page = page,
            limit = limit,
            extended = "full,cloud9,colors",
        )
        return response.body()
    }

    override suspend fun getComments(
        page: Int,
        limit: Int,
    ): List<CommentAllDto> {
        val response = usersApi.getUsersComments(
            id = "me",
            page = page,
            limit = limit,
            commentType = "all",
            type = "all",
            extended = "full,cloud9,colors",
            includeReplies = null,
        )
        return response.body()
    }
}
