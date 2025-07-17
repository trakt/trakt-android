package tv.trakt.trakt.tv.helpers.extensions

import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import java.util.Locale
import java.util.Locale.ROOT

fun Int.thousandsFormat(): String {
    return if (this >= 1_000_000) {
        String.format(ROOT, "%.1fM", this / 1_000_000F)
    } else if (this >= 1000) {
        String.format(ROOT, "%.1fK", this / 1000F)
    } else {
        this.toString()
    }
}

/**
 * Formats a duration in minutes into a human-readable string.
 */
fun Long.durationFormat(locale: Locale = Locale.US): String {
    val days = this / (60 * 24)
    val hours = (this % (60 * 24)) / 60
    val minutes = this % 60

    val measures = mutableListOf<Measure>()
    if (days > 0) {
        measures.add(Measure(days, MeasureUnit.DAY))
    }
    if (hours > 0) {
        measures.add(Measure(hours, MeasureUnit.HOUR))
    }
    if (minutes > 0 || measures.isEmpty()) {
        measures.add(Measure(minutes, MeasureUnit.MINUTE))
    }

    val format = MeasureFormat.getInstance(locale, FormatWidth.NARROW)
    return format.formatMeasures(*measures.toTypedArray())
}
