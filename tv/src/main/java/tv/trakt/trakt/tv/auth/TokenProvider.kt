package tv.trakt.trakt.tv.auth

import tv.trakt.trakt.tv.auth.model.TraktAccessToken

internal interface TokenProvider {
    suspend fun saveToken(token: TraktAccessToken)

    suspend fun getToken(): TraktAccessToken?

    suspend fun clear()
}
