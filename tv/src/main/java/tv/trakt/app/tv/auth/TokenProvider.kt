package tv.trakt.app.tv.auth

import tv.trakt.app.tv.auth.model.TraktAccessToken

internal interface TokenProvider {
    suspend fun saveToken(token: TraktAccessToken)

    suspend fun getToken(): TraktAccessToken?

    suspend fun clear()
}
