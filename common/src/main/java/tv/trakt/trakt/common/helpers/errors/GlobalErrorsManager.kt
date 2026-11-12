package tv.trakt.trakt.common.helpers.errors

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.getHttpCode

/**
 * A listener for global errors that can be emitted and observed across the application.
 */
interface GlobalErrorsManager {
    /**
     * Emits a global error to be observed by listeners.
     */
    fun tryEmit(error: Exception)

    /**
     * Emits the error when it is a VIP limit error.
     * Returns true when the error was emitted, so the caller can skip its own error handling.
     */
    fun tryEmitVipLimit(error: Exception): Boolean {
        if (error.getHttpCode() != HTTP_ERROR_TRAKT_VIP_LIMIT) return false
        tryEmit(error)
        return true
    }

    /**
     * Observes the global error stream.
     * Listeners can collect this flow to receive updates on global errors.
     * The flow emits an Exception or null if there are no errors.
     */
    fun observe(): Flow<Exception?>

    /**
     * Clears the current global error, effectively resetting the error state.
     * After calling this method, observers will receive null indicating that there are no errors.
     */
    fun clear()
}
