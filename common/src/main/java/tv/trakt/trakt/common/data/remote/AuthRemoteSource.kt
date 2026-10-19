package tv.trakt.trakt.common.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import org.openapitools.client.apis.OauthApi
import org.openapitools.client.models.PostOauthDeviceCodeRequest
import org.openapitools.client.models.PostOauthDeviceTokenRequest
import tv.trakt.trakt.common.BuildConfig
import tv.trakt.trakt.common.auth.model.AuthDeviceCode
import tv.trakt.trakt.common.auth.model.AuthDeviceTokenCode
import tv.trakt.trakt.common.auth.model.AuthDeviceTokenState
import tv.trakt.trakt.common.auth.model.AuthDeviceTokenState.Failure
import tv.trakt.trakt.common.auth.model.AuthDeviceTokenState.Success
import tv.trakt.trakt.common.auth.model.TraktAccessToken
import tv.trakt.trakt.common.data.request.TokenExchangeRequest
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import kotlin.time.Duration.Companion.seconds

class AuthRemoteSource(
    private val baseUrl: String,
    private val oauthApi: OauthApi,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) {
    private val httpClient = HttpClient(httpClientEngine) {
        httpClientConfig.invoke(this)
    }

    suspend fun getAccessToken(
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ): TraktAccessToken {
        val request = TokenExchangeRequest(
            code = code,
            clientId = BuildConfig.TRAKT_API_KEY,
            redirectUri = redirectUri,
            codeVerifier = codeVerifier,
            grantType = "authorization_code",
        )

        return httpClient.post {
            url("${baseUrl}oauth/token")
            setBody(request)
        }.body<TraktAccessToken>()
    }

    suspend fun getDeviceCode(): AuthDeviceCode {
        val request = PostOauthDeviceCodeRequest(
            clientId = BuildConfig.TRAKT_API_KEY,
        )
        val response = oauthApi.postOauthDeviceCode(request).body()
        return AuthDeviceCode(
            deviceCode = response.deviceCode,
            userCode = response.userCode,
            expiresIn = response.expiresIn.seconds,
            expiresAt = nowUtc().plusSeconds(response.expiresIn.toLong()),
            interval = response.interval.seconds,
            url = response.verificationUrl,
        )
    }

    suspend fun getDeviceToken(deviceCode: String): AuthDeviceTokenState {
        val request = PostOauthDeviceTokenRequest(
            code = deviceCode,
            clientId = BuildConfig.TRAKT_API_KEY,
        )

        return try {
            val body = oauthApi.postOauthDeviceToken(request).body()
            val token = TraktAccessToken(
                accessToken = body.accessToken,
                expiresIn = body.expiresIn.toLong(),
                refreshToken = body.refreshToken,
                createdAt = body.createdAt.toLong(),
            )
            Success(token)
        } catch (error: Exception) {
            error.rethrowCancellation()
            val httpCode = (error as? ResponseException)?.response?.status?.value ?: 0
            Failure(
                code = AuthDeviceTokenCode.fromHttpCode(httpCode),
            )
        }
    }
}
