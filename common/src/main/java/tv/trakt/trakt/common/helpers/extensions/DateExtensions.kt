package tv.trakt.trakt.common.helpers.extensions

import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun nowLocal(): ZonedDateTime = ZonedDateTime.now()

// Misc

fun String.toZonedDateTime(): ZonedDateTime {
    // Current Nitro endpoints pattern is "2025-06-23 03:24:23.000000"
    val nitroFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(UTC)
    return when {
        else -> runCatching {
            ZonedDateTime.parse(this)
        }.getOrElse {
            ZonedDateTime.parse(this, nitroFormatter).withFixedOffsetZone()
        }
    }
}
