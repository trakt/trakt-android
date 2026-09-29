package tv.trakt.trakt.core.comments.ui.richtext

private val escapedChars = setOf('\\', '*', '_', '[', ']', '`')
private val blockStartChars = setOf('#', '>', '-', '+')
private val orderedListMarker = Regex("""^(\d+)([.)])(?=\s|$)""")

private sealed interface Mark {
    val open: String
    val close: String

    data object Spoiler : Mark {
        override val open = "[spoiler]"
        override val close = "[/spoiler]"
    }

    data class Link(
        val href: String,
    ) : Mark {
        override val open = "["
        override val close = "](${href.replace(" ", "%20").replace(")", "%29")})"
    }

    data object Bold : Mark {
        override val open = "**"
        override val close = "**"
    }

    data object Italic : Mark {
        override val open = "*"
        override val close = "*"
    }
}

internal fun List<RichLine>.toMarkdown(): String {
    return mapIndexed { index, line ->
        val previous = getOrNull(index - 1)
        val separator = when {
            previous == null -> ""
            previous.type != line.type && previous.text.isNotBlank() && line.text.isNotBlank() -> "\n\n"
            else -> "\n"
        }
        separator + line.toMarkdown()
    }.joinToString("").trim()
}

private fun RichLine.toMarkdown(): String {
    val inline = runs.normalizeWhitespace().toInlineMarkdown()

    return when (type) {
        RichBlockType.Bullet -> "- $inline"
        RichBlockType.Quote -> "> $inline"
        RichBlockType.Paragraph -> inline.escapeBlockStart()
    }
}

/**
 * Keeps paragraph text from being read as a heading, quote or list. Ordered markers are escaped
 * on the delimiter because a backslash before a digit is rendered literally.
 */
private fun String.escapeBlockStart(): String {
    val content = trimStart()
    val indent = substring(0, length - content.length)

    return indent + when {
        content.firstOrNull() in blockStartChars -> "\\$content"
        else -> orderedListMarker.replace(content) { "${it.groupValues[1]}\\${it.groupValues[2]}" }
    }
}

/**
 * Markdown emphasis cannot start or end on whitespace, so a space only keeps the styles
 * shared by the visible characters on both of its sides.
 */
private fun List<RichRun>.normalizeWhitespace(): List<RichRun> {
    val chars = flatMap { run -> run.text.map { it to run.style } }
    val styles = chars.mapIndexed { index, (char, style) ->
        if (!char.isWhitespace()) return@mapIndexed style

        val before = chars.subList(0, index).lastOrNull { !it.first.isWhitespace() }?.second
        val after = chars.subList(index + 1, chars.size).firstOrNull { !it.first.isWhitespace() }?.second
        if (before == null || after == null) RichStyle() else before.intersect(after)
    }

    return chars.indices
        .fold(emptyList()) { runs, index ->
            val char = chars[index].first.toString()
            val style = styles[index]
            val last = runs.lastOrNull()
            when (last?.style) {
                style -> runs.dropLast(1) + last.copy(text = last.text + char)
                else -> runs + RichRun(char, style)
            }
        }
}

private fun RichStyle.intersect(other: RichStyle) =
    RichStyle(
        bold = bold && other.bold,
        italic = italic && other.italic,
        spoiler = spoiler && other.spoiler,
        href = href.takeIf { it == other.href },
    )

private fun RichStyle.toMarks(): List<Mark> {
    return listOfNotNull(
        Mark.Spoiler.takeIf { spoiler },
        href?.let { Mark.Link(it) },
        Mark.Bold.takeIf { bold },
        Mark.Italic.takeIf { italic },
    )
}

private fun List<RichRun>.toInlineMarkdown(): String {
    val builder = StringBuilder()
    val open = fold(emptyList<Mark>()) { stack, run ->
        val target = run.style.toMarks()
        val shared = stack.zip(target).takeWhile { (a, b) -> a == b }.size

        stack.drop(shared).asReversed().forEach { builder.append(it.close) }
        target.drop(shared).forEach { builder.append(it.open) }
        builder.append(run.text.escapeMarkdown())

        target
    }
    open.asReversed().forEach { builder.append(it.close) }

    return builder.toString()
}

private fun String.escapeMarkdown(): String {
    return fold(StringBuilder()) { builder, char ->
        if (char in escapedChars) builder.append('\\')
        builder.append(char)
    }.toString()
}
