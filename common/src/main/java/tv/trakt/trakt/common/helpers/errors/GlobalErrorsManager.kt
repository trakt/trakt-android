package tv.trakt.trakt.common.helpers.errors

import kotlinx.coroutines.flow.Flow

/**
 * A listener for global errors that can be emitted and observed across the application.
 */
interface GlobalErrorsManager {
    /**
     * Emits a global error to be observed by listeners.
     */
    fun tryEmit(error: Exception)

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
