package tv.trakt.trakt.common.networking.helpers

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DefaultCacheMarkerProvider : CacheMarkerProvider {
    private val mutex = Mutex()
    private var marker: Long = nowUtcInstant().toEpochMilli()

    override suspend fun getMarker(): String {
        return mutex.withLock {
            marker.toString()
        }
    }

    override suspend fun invalidate() {
        mutex.withLock {
            marker = nowUtcInstant().toEpochMilli()
        }
    }
}
