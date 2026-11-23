package tv.trakt.trakt.ui.components.chips

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

private const val MAX_PINGS_PER_BURST = 3
private val PingMemory = 7.days
private val BurstWindow = 10.seconds

internal object NewReleasePings {
    private val claimedAt = mutableMapOf<Any, Long>()
    private var burstStartedAt = 0L
    private var burstCount = 0

    @Synchronized
    fun claim(
        key: Any,
        nowMillis: Long = nowUtcInstant().toEpochMilli(),
    ): Int? {
        claimedAt.values.removeAll { nowMillis - it >= PingMemory.inWholeMilliseconds }
        if (key in claimedAt) return null

        if (nowMillis - burstStartedAt > BurstWindow.inWholeMilliseconds) {
            burstStartedAt = nowMillis
            burstCount = 0
        }
        if (burstCount >= MAX_PINGS_PER_BURST) return null

        claimedAt[key] = nowMillis
        return burstCount++
    }
}
