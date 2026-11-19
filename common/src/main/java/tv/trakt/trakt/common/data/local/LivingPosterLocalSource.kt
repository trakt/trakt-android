package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.livingposter.LivingPoster
import tv.trakt.trakt.common.model.livingposter.LivingPosterIndex
import java.time.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

private val RETENTION = 24.hours.toJavaDuration()

class LivingPosterLocalSource {
    private val mutex = Mutex()
    private var index: Entry<LivingPosterIndex>? = null
    private val posters = mutableMapOf<String, Entry<LivingPoster>>()

    /** The cached index, or null when missing or older than the retention window. */
    suspend fun getIndex(now: Instant): LivingPosterIndex? {
        return mutex.withLock { index?.valueAt(now) }
    }

    suspend fun setIndex(
        value: LivingPosterIndex,
        now: Instant,
    ) {
        mutex.withLock { index = Entry(value, now) }
    }

    /** The cached manifest, or null when missing or older than the retention window. */
    suspend fun getPoster(
        baseUrl: String,
        now: Instant,
    ): LivingPoster? {
        return mutex.withLock {
            val entry = posters[baseUrl] ?: return@withLock null
            entry.valueAt(now) ?: run {
                posters.remove(baseUrl)
                null
            }
        }
    }

    suspend fun setPoster(
        baseUrl: String,
        poster: LivingPoster,
        now: Instant,
    ) {
        mutex.withLock { posters[baseUrl] = Entry(poster, now) }
    }

    private data class Entry<T>(
        val value: T,
        val storedAt: Instant,
    ) {
        fun valueAt(now: Instant): T? {
            return value.takeIf { now.isBefore(storedAt.plus(RETENTION)) }
        }
    }
}
