package tv.trakt.trakt.common.helpers.extensions

import android.graphics.Typeface
import android.icu.text.CompactDecimalFormat
import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import java.util.Locale
import kotlin.math.abs

/**
 * Formats an integer into a compact string representation using thousands (e.g., 1.2K for 1200).
 */
private fun Int.thousandsFormat(
    locale: Locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault(),
): String {
    if (this < 1000) return this.toString()
    val fmt = CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT)
    return fmt.format(this)
}

/**
 * Composable function that formats an integer into a compact string representation using thousands and remembers the result.
 */
@Composable
fun rememberThousandsFormat(count: Int): String {
    val configuration = LocalConfiguration.current
    return remember(count, configuration) {
        val configurationLocale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        count.thousandsFormat(configurationLocale)
    }
}

/**
 * Formats a duration in minutes into a human-readable string.
 */
fun Long.durationFormat(
    locale: Locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault(),
): String {
    val magnitude = abs(this)
    val days = magnitude / (60 * 24)
    val hours = (magnitude % (60 * 24)) / 60
    val minutes = magnitude % 60

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
    val formatted = format.formatMeasures(*measures.toTypedArray()).capitalize()
    return if (this < 0) "-$formatted" else formatted
}

/**
 * Composable function that formats a duration in minutes into a human-readable string and remembers the result.
 * If the duration is null, it returns "N/A".
 */
@Composable
fun rememberDurationFormat(
    duration: Long?,
    spaces: Boolean = true,
): String {
    if (duration == null) {
        return "N/A"
    }

    val configuration = LocalConfiguration.current
    return remember(duration, configuration) {
        val configurationLocale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        val format = duration.durationFormat(configurationLocale)
        when {
            spaces -> format
            else -> format.replace(" ", "")
        }
    }
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

/**
 * Capitalizes the first letter of the string, leaving the rest unchanged.
 * For example, "hello world" becomes "Hello world".
 */
fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
