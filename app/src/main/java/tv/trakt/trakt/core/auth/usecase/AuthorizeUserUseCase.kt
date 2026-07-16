package tv.trakt.trakt.core.auth.usecase

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

internal class AuthorizeUserUseCase(
    private val remoteSource: AuthRemoteDataSource,
    private val tokenProvider: TokenProvider,
) {
    suspend fun authorizeByCode(
        code: String,
        codeVerifier: String?,
    ) {
        val token = remoteSource.getAccessToken(code, codeVerifier)
        tokenProvider.saveToken(token)

        delay(500.milliseconds) // Small delay to ensure token is stored before proceeding.
        Timber.d("Received and stored access token!")
    }
}
