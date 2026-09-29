package tv.trakt.trakt.core.comments.ui.richtext

private const val SPOILER_OPEN = "[spoiler]"
private const val SPOILER_CLOSE = "[/spoiler]"

private val escapableChars = setOf('\\', '`', '*', '_', '[', ']', '(', ')', '#', '>', '+', '-', '.', '!', '~')

private val bulletBlock = Regex("""^\s*[-*+]\s+(.*)$""")
private val quoteBlock = Regex("""^>\s?(.*)$""")
private val link = Regex("""\[((?:\\.|[^\]\\])*)]\(([^\s)]+)\)""")
private val multilineSpoiler = Regex(
    pattern = """\[spoiler](.*?)\[/spoiler]""",
    options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
)

private enum class InlineMark {
    Bold,
    Italic,
    Spoiler,
}

private sealed interface Token {
    data class Text(
        val value: String,
    ) : Token

    data class Toggle(
        val mark: InlineMark,
        val raw: String,
    ) : Token

    data class Link(
        val href: String,
        val label: List<Token>,
    ) : Token
}

/**
 * Inverse of [toMarkdown] for the subset the editor supports. Anything else stays literal text,
 * so comments written elsewhere load without losing content.
 */
internal fun String.parseMarkdown(): List<RichLine> {
    return splitMultilineSpoilers()
        .lines()
        .map { it.toRichLine() }
}

private fun String.splitMultilineSpoilers(): String {
    return multilineSpoiler.replace(this) { match ->
        match.groupValues[1]
            .lines()
            .joinToString("\n") { "$SPOILER_OPEN$it$SPOILER_CLOSE" }
    }
}

private fun String.toRichLine(): RichLine {
    val bullet = bulletBlock.matchEntire(this)
    val quote = quoteBlock.matchEntire(this)
    val (type, content) = when {
        bullet != null -> RichBlockType.Bullet to bullet.groupValues[1]
        quote != null -> RichBlockType.Quote to quote.groupValues[1]
        else -> RichBlockType.Paragraph to this
    }

    return RichLine(
        type = type,
        runs = content.tokenize().toRuns(RichStyle()),
    )
}

private fun String.tokenize(): List<Token> {
    val tokens = mutableListOf<Token>()
    val literal = StringBuilder()
    var index = 0

    fun emit(token: Token) {
        if (literal.isNotEmpty()) tokens += Token.Text(literal.toString())
        literal.clear()
        tokens += token
    }

    while (index < length) {
        val char = this[index]
        val linkMatch = if (char == '[') link.matchAt(this, index) else null

        when {
            char == '\\' && getOrNull(index + 1) in escapableChars -> {
                literal.append(this[index + 1])
                index += 2
            }

            startsWith(SPOILER_OPEN, index, ignoreCase = true) -> {
                emit(Token.Toggle(InlineMark.Spoiler, substring(index, index + SPOILER_OPEN.length)))
                index += SPOILER_OPEN.length
            }

            startsWith(SPOILER_CLOSE, index, ignoreCase = true) -> {
                emit(Token.Toggle(InlineMark.Spoiler, substring(index, index + SPOILER_CLOSE.length)))
                index += SPOILER_CLOSE.length
            }

            linkMatch != null -> {
                emit(Token.Link(href = linkMatch.groupValues[2], label = linkMatch.groupValues[1].tokenize()))
                index = linkMatch.range.last + 1
            }

            char == '*' || char == '_' -> {
                val end = indexOfFirst(index) { it != char }
                val delimiters = emphasisToggles(start = index, end = end)
                if (delimiters.isEmpty()) literal.append(substring(index, end)) else delimiters.forEach(::emit)
                index = end
            }

            else -> {
                literal.append(char)
                index++
            }
        }
    }
    if (literal.isNotEmpty()) tokens += Token.Text(literal.toString())

    return tokens.balanced()
}

/**
 * A delimiter run only toggles emphasis when it touches text, and underscores never toggle
 * inside a word, so `5 * 3` and `snake_case` stay literal.
 */
private fun String.emphasisToggles(
    start: Int,
    end: Int,
): List<Token.Toggle> {
    val char = this[start]
    val count = end - start
    val before = getOrNull(start - 1)
    val after = getOrNull(end)

    val canOpen = after != null && !after.isWhitespace()
    val canClose = before != null && !before.isWhitespace()
    val isIntraword = char == '_' && before?.isLetterOrDigit() == true && after?.isLetterOrDigit() == true

    if (!(canOpen || canClose) || isIntraword || count > 3) return emptyList()

    val single = char.toString()
    return listOfNotNull(
        Token.Toggle(InlineMark.Bold, single.repeat(2)).takeIf { count >= 2 },
        Token.Toggle(InlineMark.Italic, single).takeIf { count != 2 },
    )
}

/**
 * Unclosed delimiters are kept as literal text instead of styling the rest of the line.
 */
private fun List<Token>.balanced(): List<Token> {
    val unmatched = InlineMark.entries
        .mapNotNull { mark ->
            val toggles = indices.filter { (this[it] as? Token.Toggle)?.mark == mark }
            toggles.lastOrNull().takeIf { toggles.size % 2 == 1 }
        }
        .toSet()

    return mapIndexed { index, token ->
        when {
            index in unmatched && token is Token.Toggle -> Token.Text(token.raw)
            else -> token
        }
    }
}

private fun List<Token>.toRuns(base: RichStyle): List<RichRun> {
    return fold(base to emptyList<RichRun>()) { (style, runs), token ->
        when (token) {
            is Token.Text -> {
                style to runs.append(RichRun(token.value, style))
            }

            is Token.Toggle -> {
                style.toggle(token.mark) to runs
            }

            is Token.Link -> {
                val linked = token.label.toRuns(style.copy(href = token.href))
                style to linked.fold(runs) { merged, run -> merged.append(run) }
            }
        }
    }.second
}

private fun List<RichRun>.append(run: RichRun): List<RichRun> {
    val last = lastOrNull()
    return when (last?.style) {
        run.style -> dropLast(1) + last.copy(text = last.text + run.text)
        else -> this + run
    }
}

private fun RichStyle.toggle(mark: InlineMark): RichStyle {
    return when (mark) {
        InlineMark.Bold -> copy(bold = !bold)
        InlineMark.Italic -> copy(italic = !italic)
        InlineMark.Spoiler -> copy(spoiler = !spoiler)
    }
}

private fun String.indexOfFirst(
    from: Int,
    predicate: (Char) -> Boolean,
): Int {
    return (from until length).firstOrNull { predicate(this[it]) } ?: length
}
