package tv.trakt.trakt.common.core.user.data.remote

import org.openapitools.client.apis.SyncApi
import org.openapitools.client.apis.UsersApi
import org.openapitools.client.models.PutUsersSaveSettingsRequest
import org.openapitools.client.models.PutUsersSaveSettingsRequestBrowsing
import org.openapitools.client.models.PutUsersSaveSettingsRequestUser
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.SyncLibraryMediaDto
import tv.trakt.trakt.common.networking.UserWatchingDto
import tv.trakt.trakt.common.networking.WatchedShowDto
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

    override suspend fun getWatchedMovies(): Map<String, List<String>> {
        val response = usersApi.getUsersWatchedMinimalMovies(
            id = "me",
            extended = "min",
        )

        return response.body()
    }

    override suspend fun getWatchedShows(): List<WatchedShowDto> {
        val response = usersApi.getUsersWatchedShows(
            id = "me",
            extended = null,
            hidden = null,
            specials = true,
            countSpecials = null,
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

}
