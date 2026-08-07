package tv.trakt.trakt.common.helpers.extensions

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats a whole-number percentage (e.g. 94) as a locale-aware percent
 * string (e.g. "94%" in English, "94 %" in German), rather than
 * concatenating a literal "%" that assumes English's no-space convention.
 */
fun Int.toPercentString(locale: Locale): String =
    NumberFormat.getPercentInstance(locale).format(this / 100.0)
