package tv.trakt.trakt.common.helpers.extensions

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.crashlytics
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import timber.log.Timber
import tv.trakt.trakt.common.BuildConfig
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
 * Records an error to Firebase Crashlytics if it's not in the ignored exceptions list.
 * Ignores errors in DEBUG builds and certain HTTP errors (400-401).
 */
fun Timber.Forest.recordError(
    error: Exception,
    keysValues: CustomKeysAndValues? = null,
) {
    Timber.e(error)

    if (BuildConfig.DEBUG) {
        Timber.d("Not recording error to Crashlytics in DEBUG build.")
        return
    }

    if (ignoredExceptions.any { it.isInstance(error) }) {
        Timber.d("Ignored error type: ${error::class.java.simpleName}")
        return
    }

    if (error is ClientRequestException) {
        val httpCode = error.response.status.value
        if (httpCode in 400..401) {
            Timber.d("Ignored HTTP error with status code: $httpCode")
            return
        }
    }

    if (keysValues != null) {
        Firebase.crashlytics.recordException(error, keysValues)
    } else {
        Firebase.crashlytics.recordException(error)
    }

    Timber.d("Recorded error to Crashlytics: $error")
}
