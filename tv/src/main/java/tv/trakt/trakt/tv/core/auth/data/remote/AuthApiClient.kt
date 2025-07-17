package tv.trakt.trakt.tv.core.auth.data.remote

import io.ktor.client.plugins.ResponseException
import org.openapitools.client.apis.OauthApi
import org.openapitools.client.models.PostOauthDeviceCodeRequest
import org.openapitools.client.models.PostOauthDeviceTokenRequest
import tv.trakt.trakt.tv.BuildConfig
import tv.trakt.trakt.tv.auth.model.TraktAccessToken
import tv.trakt.trakt.tv.core.auth.model.AuthDeviceCode
import tv.trakt.trakt.tv.core.auth.model.AuthDeviceTokenCode
import tv.trakt.trakt.tv.core.auth.model.AuthDeviceTokenState
import tv.trakt.trakt.tv.core.auth.model.AuthDeviceTokenState.Failure
import tv.trakt.trakt.tv.core.auth.model.AuthDeviceTokenState.Success
import tv.trakt.trakt.tv.helpers.extensions.nowUtc
import kotlin.time.Duration.Companion.seconds

internal class AuthApiClient(
    private val api: OauthApi,
) : AuthRemoteDataSource {
    override suspend fun getDeviceCode(): AuthDeviceCode {
        val request = PostOauthDeviceCodeRequest(
            clientId = BuildConfig.TRAKT_API_KEY,
        )
        val response = api.postOauthDeviceCode(request).body()
        return AuthDeviceCode(
            deviceCode = response.deviceCode,
            userCode = response.userCode,
            expiresIn = response.expiresIn.seconds,
            expiresAt = nowUtc().plusSeconds(response.expiresIn.toLong()),
            interval = response.interval.seconds,
            url = response.verificationUrl,
        )
    }

    override suspend fun getDeviceToken(deviceCode: String): AuthDeviceTokenState {
        val request = PostOauthDeviceTokenRequest(
            code = deviceCode,
            clientId = BuildConfig.TRAKT_API_KEY,
            clientSecret = BuildConfig.TRAKT_API_SECRET,
        )

        try {
            val response = api.postOauthDeviceToken(request)
            val body = response.body()
            val token = TraktAccessToken(
                accessToken = body.accessToken,
                expiresIn = body.expiresIn.toLong(),
                refreshToken = body.refreshToken,
                createdAt = body.createdAt.toLong(),
            )
            return Success(token)
        } catch (error: Exception) {
            val httpCode = (error as? ResponseException)?.response?.status?.value ?: 0
            return Failure(
                code = AuthDeviceTokenCode.fromHttpCode(httpCode),
            )
        }
    }
}
