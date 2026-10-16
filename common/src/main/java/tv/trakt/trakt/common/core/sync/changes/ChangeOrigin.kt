package tv.trakt.trakt.common.core.sync.changes

import java.util.UUID

/**
 * Identifies who published a [DataChange], so the publisher can skip its own changes.
 * Backed by a string so it survives WorkManager input data.
 */
@JvmInline
value class ChangeOrigin(
    val key: String,
) {
    companion object {
        val RatePrompt = ChangeOrigin("rate_prompt")
        val TodayStories = ChangeOrigin("today_stories")

        fun unique(): ChangeOrigin = ChangeOrigin(UUID.randomUUID().toString())
    }
}
