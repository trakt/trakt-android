package tv.trakt.trakt.analytics.helpers.crashlytics

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import io.ktor.client.plugins.HttpRequestTimeoutException
import timber.log.Timber
import tv.trakt.trakt.BuildConfig
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

private val ignoredExceptions = arrayOf(
    CancellationException::class, // Ignore cancellations
    HttpRequestTimeoutException::class, // Ignore HTTP timeouts
    UnknownHostException::class, // Ignore no internet connection
)

/**
 * Records the given [error] to Crashlytics.
 */
fun Timber.Forest.recordError(error: Exception) {
    Timber.e(error)

    if (ignoredExceptions.any { it.isInstance(error) }) {
        return
    }

    if (!BuildConfig.DEBUG) {
        Firebase.crashlytics.recordException(error)
        Timber.d("Recorded error to Crashlytics: $error")
    }
}

/**
 * Records the given [error] to Crashlytics only if it is of type [T].
 */
inline fun <reified T : Exception> Timber.Forest.recordErrorIf(error: Exception) {
    Timber.e(error)

    if (error !is T) {
        return
    }

    if (!BuildConfig.DEBUG) {
        Firebase.crashlytics.recordException(error)
        Timber.d("Recorded error to Crashlytics: $error")
    }
}
