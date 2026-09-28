package tv.trakt.trakt.common.helpers.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

private class MarkdownStyle(
    val linkColor: Color,
    val mentionColor: Color?,
)

private val HEADING = Regex("^#{1,6}\\s+(.*)$")
private val BULLET = Regex("^\\s*[-*+]\\s+(.*)$")
private val ORDERED = Regex("^\\s*(\\d+)[.)]\\s+(.*)$")
private val QUOTE = Regex("^>\\s?(.*)$")
private val MENTION = Regex("@[a-zA-Z0-9_]+")

private val INLINE = Regex(
    "\\\\(?<esc>[\\\\`*_~\\[\\]()#>+.!-])" +
        "|`(?<code>[^`]+)`" +
        "|\\[(?<label>[^\\]]+)]\\((?<href>https?://[^\\s)]+)\\)" +
        "|(?<url>https?://[^\\s<>()]*[^\\s<>().,;:!?])" +
        "|\\*\\*(?<bold>.+?)\\*\\*" +
        "|__(?<bold2>.+?)__" +
        "|~~(?<strike>.+?)~~" +
        "|\\*(?<italic>[^*\\s](?:.*?[^*\\s])?)\\*" +
        "|(?<![\\p{L}\\p{N}_])_(?<italic2>[^_\\s](?:.*?[^_\\s])?)_(?![\\p{L}\\p{N}_])",
)

/** Links are limited to http(s). Pass [mentionColor] to also highlight @mentions. */
fun String.toMarkdownText(
    linkColor: Color,
    mentionColor: Color? = null,
): AnnotatedString {
    val style = MarkdownStyle(linkColor, mentionColor)
    return buildAnnotatedString {
        lines().forEachIndexed { index, line ->
            if (index > 0) append('\n')
            appendBlock(line.trimEnd(), style)
        }
    }
}

private fun AnnotatedString.Builder.appendBlock(
    line: String,
    style: MarkdownStyle,
) {
    HEADING.matchEntire(line)?.let {
        withStyle(SpanStyle(fontWeight = Bold)) { appendInline(it.groupValues[1], style) }
        return
    }
    BULLET.matchEntire(line)?.let {
        append("•  ")
        appendInline(it.groupValues[1], style)
        return
    }
    ORDERED.matchEntire(line)?.let {
        append("${it.groupValues[1]}.  ")
        appendInline(it.groupValues[2], style)
        return
    }
    QUOTE.matchEntire(line)?.let {
        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { appendInline(it.groupValues[1], style) }
        return
    }
    appendInline(line, style)
}

private fun AnnotatedString.Builder.appendInline(
    text: String,
    style: MarkdownStyle,
    links: Boolean = true,
) {
    var cursor = 0
    for (match in INLINE.findAll(text)) {
        appendPlain(text.substring(cursor, match.range.first), style)
        cursor = match.range.last + 1

        val groups = match.groups
        val escaped = groups["esc"]?.value
        val code = groups["code"]?.value
        val label = groups["label"]?.value
        val href = groups["href"]?.value
        val url = groups["url"]?.value
        val bold = groups["bold"]?.value ?: groups["bold2"]?.value
        val strike = groups["strike"]?.value
        val italic = groups["italic"]?.value ?: groups["italic2"]?.value

        when {
            escaped != null -> append(escaped)
            code != null -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(code) }
            label != null && href != null -> appendLink(label, href, style, links)
            url != null -> appendLink(url, url, style, links)
            bold != null -> withStyle(SpanStyle(fontWeight = Bold)) { appendInline(bold, style, links) }
            strike != null -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                appendInline(strike, style, links)
            }
            italic != null -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInline(italic, style, links)
            }
        }
    }
    appendPlain(text.substring(cursor), style)
}

private fun AnnotatedString.Builder.appendLink(
    label: String,
    url: String,
    style: MarkdownStyle,
    linksEnabled: Boolean,
) {
    if (!linksEnabled) {
        append(label)
        return
    }
    val linkStyle = SpanStyle(color = style.linkColor, textDecoration = TextDecoration.Underline)
    withLink(LinkAnnotation.Url(url, TextLinkStyles(style = linkStyle))) {
        appendInline(label, style, links = false)
    }
}

private fun AnnotatedString.Builder.appendPlain(
    text: String,
    style: MarkdownStyle,
) {
    val mentionColor = style.mentionColor
    if (mentionColor == null) {
        append(text)
        return
    }

    var cursor = 0
    for (match in MENTION.findAll(text)) {
        append(text.substring(cursor, match.range.first))
        withStyle(SpanStyle(color = mentionColor, fontWeight = W500)) { append(match.value) }
        cursor = match.range.last + 1
    }
    append(text.substring(cursor))
}
