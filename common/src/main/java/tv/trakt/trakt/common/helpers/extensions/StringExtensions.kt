package tv.trakt.trakt.common.helpers.extensions

import android.graphics.Typeface
import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextDecoration
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

/**
 * Converts the first letter of each word in the string to uppercase.
 * For example, "hello world" becomes "Hello World".
 */
fun String.uppercaseWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

/**
 * Converts an Android Spanned string to a Compose AnnotatedString,
 * preserving styles such as bold, italic, underline, and foreground color.
 */
fun Spanned.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        val spanned = this@toAnnotatedString
        append(spanned.toString())
        getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = getSpanStart(span)
            val end = getSpanEnd(span)
            when (span) {
                is StyleSpan -> when (span.style) {
                    Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    Typeface.BOLD_ITALIC -> addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                        start,
                        end,
                    )
                }
                is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                is ForegroundColorSpan -> addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
            }
        }
    }
}

/**
 * Converts a string with custom markup (e.g., **bold**) into an AnnotatedString with the specified style applied to the marked text.
 */
fun customAnnotatedString(
    string: String,
    style: SpanStyle,
): AnnotatedString {
    return buildAnnotatedString {
        val regex = "\\*\\*(.*?)\\*\\*".toRegex()
        var lastIndex = 0
        regex.findAll(string).forEach { result ->
            append(string.substring(lastIndex, result.range.first))
            withStyle(
                style.copy(
                    fontWeight = FontWeight.W600,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append(result.groupValues[1])
            }
            lastIndex = result.range.last + 1
        }
        append(string.substring(lastIndex))
    }
}

/**
 * Replaces **bold** with <b>bold</b> in the string, converting custom markdown to HTML-like tags.
 * Replaces *text* with <i>text</i> for italics, and __text__ with <u>text</u> for underline.
 * Replaces bullet list items (lines starting with - or *) with • prefixed lines.
 */
fun String.replaceMarkdown(): String {
    return this.replace("\\*\\*(.*?)\\*\\*".toRegex(), "<b>$1</b>")
        .replace("\\*(.*?)\\*".toRegex(), "<i>$1</i>&nbsp;")
        .replace("__(.*?)__".toRegex(), "<u>$1</u>")
        .replace("^[ \\t]*[-*][ \\t]+(.*)".toRegex(RegexOption.MULTILINE), "<br><br>- $1")
}
