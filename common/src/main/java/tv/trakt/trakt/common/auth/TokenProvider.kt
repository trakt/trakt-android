package tv.trakt.trakt.common.auth

import tv.trakt.trakt.common.auth.model.TraktAccessToken

interface TokenProvider {
    suspend fun saveToken(token: TraktAccessToken)

    suspend fun getToken(): TraktAccessToken?

    suspend fun clear()
}
