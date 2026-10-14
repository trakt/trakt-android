package tv.trakt.trakt.core.today.data.local

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface TodaySeenLocalDataSource {
    /** When each story group was last viewed, keyed by group key. */
    fun observeSeen(): Flow<Map<String, Instant>>

    suspend fun markSeen(
        key: String,
        seenAt: Instant,
    )

    suspend fun clear()
}
