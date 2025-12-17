package tv.trakt.trakt.common.helpers.extensions

import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.withStyle
import java.util.Locale
import java.util.Locale.ROOT

fun Int.thousandsFormat(): String {
    return if (this >= 1_000_000) {
        String.format(ROOT, "%.1fM", this / 1_000_000F).replace(".0M", "M")
    } else if (this >= 1000) {
        String.format(ROOT, "%.1fK", this / 1000F).replace(".0K", "K")
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

/**
 * Highlights people mentions (e.g., @johnlegend) in the string with the specified color.
 * Returns an AnnotatedString with mentions styled in the given color.
 */
fun String.highlightMentions(color: Color): AnnotatedString {
    return buildAnnotatedString {
        val mentionRegex = "@[a-zA-Z0-9_]+".toRegex()
        val matches = mentionRegex.findAll(this@highlightMentions)

        var lastIndex = 0

        matches.forEach { match ->
            // Add text before the mention
            if (match.range.first > lastIndex) {
                append(this@highlightMentions.substring(lastIndex, match.range.first))
            }

            // Add the highlighted mention
            withStyle(style = SpanStyle(color = color, fontWeight = W500)) {
                append(match.value)
            }

            lastIndex = match.range.last + 1
        }

        if (lastIndex < this@highlightMentions.length) {
            append(this@highlightMentions.substring(lastIndex))
        }
    }
}

fun String.uppercaseWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
