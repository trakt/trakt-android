package tv.trakt.trakt.core.auth.data.remote

import org.openapitools.client.apis.OauthApi
import org.openapitools.client.models.PostOauthTokenRequest
import tv.trakt.trakt.app.BuildConfig
import tv.trakt.trakt.common.auth.model.TraktAccessToken
import tv.trakt.trakt.core.auth.ConfigAuth

internal class AuthApiClient(
    private val api: OauthApi,
) : AuthRemoteDataSource {
    override suspend fun getAccessToken(code: String): TraktAccessToken {
        val request = PostOauthTokenRequest(
            code = code,
            clientId = BuildConfig.TRAKT_API_KEY,
            clientSecret = BuildConfig.TRAKT_API_SECRET,
            redirectUri = ConfigAuth.OAUTH_REDIRECT_URI,
            grantType = "authorization_code",
        )

        val response = api.postOauthToken(request).body()

        return TraktAccessToken(
            accessToken = response.accessToken,
            expiresIn = response.expiresIn.toLong(),
            refreshToken = response.refreshToken,
            createdAt = response.createdAt.toLong(),
        )
    }
}
