package tv.trakt.trakt.common.auth.usecase

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.data.remote.AuthRemoteSource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import kotlin.time.Duration.Companion.seconds

private val RevokeTimeout = 15.seconds

/**
 * Revokes the stored access token on the server before a local sign-out. Retries come from the
 * shared HTTP client. The call is bounded and survives the caller's cancellation, and failures
 * are only logged, so the local sign-out that follows always runs.
 */
class RevokeTokenUseCase(
    private val tokenProvider: TokenProvider,
    private val authRemoteSource: AuthRemoteSource,
) {
    suspend fun revokeToken() {
        val token = tokenProvider.getToken()?.accessToken
        if (token.isNullOrBlank()) return

        withContext(NonCancellable) {
            withTimeoutOrNull(RevokeTimeout) {
                try {
                    authRemoteSource.revokeToken(token)
                } catch (error: Exception) {
                    error.rethrowCancellation {
                        Timber.w(error, "Failed to revoke token")
                    }
                }
            } ?: Timber.w("Token revoke timed out")
        }
    }
}
