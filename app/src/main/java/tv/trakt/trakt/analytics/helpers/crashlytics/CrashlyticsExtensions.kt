package tv.trakt.trakt.analytics.helpers.crashlytics

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber
import tv.trakt.trakt.BuildConfig
import kotlin.coroutines.cancellation.CancellationException

/**
 * Records the given [error] to Crashlytics.
 */
fun Timber.Forest.recordError(error: Exception) {
    Timber.e(error)

    if (error is CancellationException) {
        return
    }

    if (BuildConfig.DEBUG) {
        Timber.d("Skipping recording error to Crashlytics in DEBUG build: $error")
        return
    }

    Firebase.crashlytics.recordException(error)
    Timber.d("Recorded error to Crashlytics: $error")
}

/**
 * Records the given [error] to Crashlytics only if it is of type [T].
 */
inline fun <reified T : Exception> Timber.Forest.recordErrorIf(error: Exception) {
    Timber.e(error)

    if (error !is T) {
        return
    }

    if (BuildConfig.DEBUG) {
        Timber.d("Skipping recording error to Crashlytics in DEBUG build: $error")
        return
    }

    Firebase.crashlytics.recordException(error)
    Timber.d("Recorded error to Crashlytics: $error")
}
