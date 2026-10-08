package tv.trakt.trakt.core.auth.usecase

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.delay
import timber.log.Timber
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.core.auth.data.remote.AuthRemoteDataSource
import kotlin.time.Duration.Companion.milliseconds

/**
 * PKCE verifier persisted alongside [authCodeKey] so it survives process death while the
 * user is away in the external browser (RFC 7636). Consumed once during the token exchange.
 */
internal val codeVerifierKey = stringPreferencesKey("code_verifier")

/**
 * Authorization code persisted so it survives process death while the user is away in the
 * external browser. Consumed once during the token exchange.
 */
internal val authCodeKey = stringPreferencesKey("auth_code")

/**
 * Redirect URI that delivered [authCodeKey]. The token exchange must send the same one.
 */
internal val authRedirectUriKey = stringPreferencesKey("auth_redirect_uri")

/**
 * Set on sign-out so the next sign-in shows the login form instead of reusing the browser's
 * Trakt session, letting the user pick another account. Cleared once a code arrives.
 */
internal val forceLoginKey = booleanPreferencesKey("force_login")

internal class AuthorizeUserUseCase(
    private val remoteSource: AuthRemoteDataSource,
    private val tokenProvider: TokenProvider,
) {
    suspend fun authorizeByCode(
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ) {
        val token = remoteSource.getAccessToken(code, codeVerifier, redirectUri)
        tokenProvider.saveToken(token)

        delay(500.milliseconds) // Small delay to ensure token is stored before proceeding.
        Timber.d("Received and stored access token!")
    }
}
