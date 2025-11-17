package tv.trakt.trakt.analytics.crashlytics

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import io.ktor.client.plugins.HttpRequestTimeoutException
import timber.log.Timber
import tv.trakt.trakt.BuildConfig
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlin.coroutines.cancellation.CancellationException

private val ignoredExceptions = arrayOf(
    CancellationException::class, // Ignore coroutine cancellations
    HttpRequestTimeoutException::class, // Ignore HTTP timeouts
    UnknownHostException::class, // Ignore no internet connection
    IOException::class, // Ignore general I/O errors
    SocketTimeoutException::class, // Ignore socket-level timeouts
    ConnectException::class, // Ignore connection failures
    SocketException::class, // Ignore general socket errors
    SSLException::class, // Ignore SSL/TLS handshake failures
)

/**
 * Records the given [error] to Crashlytics.
 */
fun Timber.Forest.recordError(error: Exception) {
    Timber.e(error)

    if (BuildConfig.DEBUG) {
        Timber.d("Not recording error to Crashlytics in DEBUG build.")
        return
    }

    if (ignoredExceptions.any { it.isInstance(error) }) {
        Timber.d("Ignored error type: ${error::class.java.simpleName}")
        return
    }

    Firebase.crashlytics.recordException(error)
    Timber.d("Recorded error to Crashlytics: $error")
}
