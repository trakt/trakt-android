package tv.trakt.trakt.core.settings.features.history.usecases

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import timber.log.Timber
import tv.trakt.trakt.common.data.remote.UserHistoryRemoteSource
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val CHUNK_SIZE = 100
private const val MAX_ATTEMPTS = 3
private val DEFAULT_RETRY_AFTER = 5.seconds

internal class CleanUpHistoryPlaysUseCase(
    private val remoteSource: UserHistoryRemoteSource,
) {
    /**
     * Removes the given plays in chunks and returns how many could not be removed.
     * A failed chunk is counted and skipped so the rest of the clean up still runs.
     */
    suspend fun cleanUp(
        playIds: List<Long>,
        onProgress: (processed: Int) -> Unit,
    ): Int {
        var processed = 0
        var failed = 0

        playIds.chunked(CHUNK_SIZE).forEach { chunk ->
            try {
                removeWithRateLimit(chunk)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    failed += chunk.size
                    Timber.recordError(error)
                }
            }
            processed += chunk.size
            onProgress(processed)
        }

        return failed
    }

    private suspend fun removeWithRateLimit(
        chunk: List<Long>,
        attempt: Int = 1,
    ) {
        try {
            remoteSource.removePlays(chunk)
        } catch (error: ClientRequestException) {
            if (error.response.status != HttpStatusCode.TooManyRequests || attempt >= MAX_ATTEMPTS) {
                throw error
            }
            delay(error.retryAfter())
            removeWithRateLimit(chunk, attempt + 1)
        }
    }

    private fun ClientRequestException.retryAfter(): Duration {
        return response.headers[HttpHeaders.RetryAfter]
            ?.toDoubleOrNull()
            ?.seconds
            ?: DEFAULT_RETRY_AFTER
    }
}
