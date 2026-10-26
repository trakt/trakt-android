package tv.trakt.trakt.common.auth.usecase

import kotlinx.coroutines.delay
import timber.log.Timber
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.data.remote.AuthRemoteSource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import kotlin.time.Duration.Companion.seconds

private const val MAX_ATTEMPTS = 3

/**
 * Revokes the stored access token on the server before a local sign-out. Failures are logged
 * and swallowed, so a server that keeps refusing never blocks the user from signing out.
 */
class RevokeTokenUseCase(
    private val tokenProvider: TokenProvider,
    private val authRemoteSource: AuthRemoteSource,
) {
    suspend fun revokeToken() {
        val token = tokenProvider.getToken()?.accessToken
        if (token.isNullOrBlank()) return

        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                authRemoteSource.revokeToken(token)
                return
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Failed to revoke token, attempt %d", attempt + 1)
                }
            }

            if (attempt < MAX_ATTEMPTS - 1) {
                delay((1L shl attempt).seconds)
            }
        }
    }
}
