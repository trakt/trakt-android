package tv.trakt.trakt.core.auth.data.remote

import tv.trakt.trakt.common.auth.model.TraktAccessToken

internal interface AuthRemoteDataSource {
    suspend fun getAccessToken(code: String): TraktAccessToken
}
