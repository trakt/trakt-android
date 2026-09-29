package tv.trakt.trakt.core.comments.ui.richtext

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.text.style.UpdateAppearance

internal class SpoilerSpan(
    private val tint: Int,
    private val blurRadius: Float,
    var isBlurred: Boolean,
) : CharacterStyle(),
    UpdateAppearance {
    override fun updateDrawState(paint: TextPaint) {
        if (isBlurred) {
            paint.setShadowLayer(blurRadius, 0F, 0F, paint.color)
            paint.color = Color.TRANSPARENT
            return
        }
        paint.bgColor = tint
    }
}

internal class MentionSpan(
    val href: String,
) : CharacterStyle(),
    UpdateAppearance {
    override fun updateDrawState(paint: TextPaint) {
        paint.isUnderlineText = true
    }
}

internal fun Spanned.hasStyleAt(
    style: Int,
    index: Int,
): Boolean {
    return getSpans(index, index + 1, StyleSpan::class.java)
        .any { it.style == style && getSpanStart(it) <= index && getSpanEnd(it) > index }
}

internal inline fun <reified T : Any> Spanned.spanAt(index: Int): T? {
    return getSpans(index, index + 1, T::class.java)
        .firstOrNull { getSpanStart(it) <= index && getSpanEnd(it) > index }
}

internal fun Spanned.styleAt(index: Int): RichStyle {
    return RichStyle(
        bold = hasStyleAt(Typeface.BOLD, index),
        italic = hasStyleAt(Typeface.ITALIC, index),
        spoiler = spanAt<SpoilerSpan>(index) != null,
        href = spanAt<MentionSpan>(index)?.href,
    )
}

internal fun Spanned.lineStarts(): List<Int> {
    return listOf(0) + indices.filter { this[it] == '\n' }.map { it + 1 }
}

internal fun Spanned.toRichLines(blockTypes: List<RichBlockType>): List<RichLine> {
    return lineStarts().mapIndexed { line, start ->
        val end = indexOf('\n', start).takeIf { it >= 0 } ?: length
        val runs = (start until end).fold(emptyList<RichRun>()) { runs, index ->
            val style = styleAt(index)
            val char = this[index].toString()
            val last = runs.lastOrNull()
            when (last?.style) {
                style -> runs.dropLast(1) + last.copy(text = last.text + char)
                else -> runs + RichRun(char, style)
            }
        }
        RichLine(type = blockTypes.getOrElse(line) { RichBlockType.Paragraph }, runs = runs)
    }
}
