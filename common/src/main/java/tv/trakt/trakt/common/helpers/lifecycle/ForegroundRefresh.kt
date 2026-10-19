package tv.trakt.trakt.common.helpers.lifecycle

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

/**
 * Emits when the app returns to the foreground, at most once per [minInterval].
 * The interval starts on collection, so a screen that has just loaded is not refreshed again.
 */
fun AppLifecycleProvider.observeForegroundRefresh(minInterval: Duration = 1.minutes): Flow<Unit> {
    return flow {
        var lastRefresh = TimeSource.Monotonic.markNow()
        observeState(FOREGROUND).collect {
            if (lastRefresh.elapsedNow() < minInterval) {
                Timber.d("Skipping foreground refresh, last refresh was ${lastRefresh.elapsedNow()} ago")
                return@collect
            }
            lastRefresh = TimeSource.Monotonic.markNow()
            emit(Unit)
            Timber.d("Foreground refresh emitted")
        }
    }
}
