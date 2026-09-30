package tv.trakt.trakt.core.comments.ui.richtext

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BulletSpan
import android.text.style.QuoteSpan
import android.text.style.StyleSpan
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText
import tv.trakt.trakt.core.comments.model.CommentMention
import tv.trakt.trakt.core.comments.ui.richtext.RichBlockType.Bullet
import tv.trakt.trakt.core.comments.ui.richtext.RichBlockType.Paragraph
import tv.trakt.trakt.core.comments.ui.richtext.RichBlockType.Quote

private const val MAX_MENTION_QUERY_LENGTH = 40

private val boldShortcut = Regex("""(?<![*\\])\*\*([^*\s](?:[^*]*[^*\s])?)\*\*$""")
private val italicShortcut = Regex("""(?<![*\\])\*([^*\s](?:[^*]*[^*\s])?)\*$""")

internal data class RichTextDecorations(
    val spoilerTint: Int,
    val blurRadius: Float,
    val bulletColor: Int,
    val bulletRadius: Int,
    val bulletGap: Int,
    val quoteColor: Int,
    val quoteStripe: Int,
    val quoteGap: Int,
)

internal data class RichTextSnapshot(
    val text: String,
    val markdown: String,
    val toolbar: RichToolbarState,
    val mentionQuery: MentionQuery?,
    val isFocused: Boolean,
)

internal enum class RichInlineStyle {
    Bold,
    Italic,
    Spoiler,
}

internal class RichTextEditText(
    context: Context,
) : AppCompatEditText(context) {
    private var isReady = false
    private var isApplying = false
    private var isTextChanging = false

    private var blockTypes: List<RichBlockType> = listOf(Paragraph)
    private var pendingStyle: RichStyle? = null
    private var pendingAt = -1

    var decorations: RichTextDecorations? = null
    var onSnapshot: (RichTextSnapshot) -> Unit = {}

    private val watcher = object : TextWatcher {
        private var edit = PendingEdit()

        override fun beforeTextChanged(
            text: CharSequence,
            start: Int,
            count: Int,
            after: Int,
        ) {
            if (isApplying) return
            isTextChanging = true
            val spanned = text as Spanned
            val line = text.subSequence(0, start).count { it == '\n' }
            edit = PendingEdit(
                start = start,
                line = line,
                removed = text.subSequence(start, start + count).toString(),
                removedStyle = if (count > 0) spanned.styleAt(start) else null,
                typesBefore = blockTypes,
                lineWasBlank = spanned.lineTextAround(start).isBlank(),
            )
        }

        override fun onTextChanged(
            text: CharSequence,
            start: Int,
            before: Int,
            count: Int,
        ) {
            if (isApplying) return
            edit = edit.copy(inserted = text.subSequence(start, start + count).toString())
        }

        override fun afterTextChanged(text: Editable) {
            if (isApplying) return
            isApplying = true
            try {
                applyEdit(text, edit)
            } finally {
                isApplying = false
                isTextChanging = false
            }
            publish()
        }
    }

    init {
        background = null
        setPadding(0, 0, 0, 0)
        gravity = Gravity.TOP or Gravity.START
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        isVerticalScrollBarEnabled = true
        addTextChangedListener(watcher)
        isReady = true
    }

    fun setPlainText(value: String) {
        isApplying = true
        setText(value)
        blockTypes = List(value.count { it == '\n' } + 1) { Paragraph }
        setSelection(value.length)
        isApplying = false
        publish()
    }

    fun setRichLines(lines: List<RichLine>) {
        isApplying = true
        setText(lines.joinToString("\n") { it.text })

        val editable = text
        if (editable != null) {
            lines.fold(0) { lineStart, line ->
                line.runs.fold(lineStart) { start, run ->
                    editable.setRunStyle(run.style, start, start + run.text.length)
                    start + run.text.length
                } + 1
            }
            blockTypes = lines.map { it.type }.ifEmpty { listOf(Paragraph) }
            editable.renderBlocks()
            setSelection(editable.length)
        }
        isApplying = false
        publish()
    }

    fun toggleInline(style: RichInlineStyle) {
        val editable = text ?: return
        val start = selectionStart.coerceAtLeast(0)
        val end = selectionEnd.coerceAtLeast(0)

        if (start == end) {
            pendingStyle = typingStyleAt(editable, start).toggle(style)
            pendingAt = start
            publish()
            return
        }

        val isApplied = (start until end)
            .filter { !editable[it].isWhitespace() }
            .ifEmpty { (start until end).toList() }
            .all { editable.styleAt(it).has(style) }

        isApplying = true
        editable.setInline(start, end, style, !isApplied)
        isApplying = false
        publish()
    }

    fun toggleBlock(type: RichBlockType) {
        val editable = text ?: return
        val firstLine = editable.lineOf(selectionStart.coerceAtLeast(0))
        val lastLine = editable.lineOf(selectionEnd.coerceAtLeast(0))
        val lines = firstLine..lastLine
        val target = if (lines.all { blockTypes.getOrNull(it) == type }) Paragraph else type

        blockTypes = blockTypes.mapIndexed { index, current -> if (index in lines) target else current }
        isApplying = true
        editable.renderBlocks()
        isApplying = false
        publish()
    }

    fun insertMention(
        mention: CommentMention,
        range: IntRange? = null,
    ) {
        val editable = text ?: return
        val start = range?.first ?: selectionStart.coerceAtLeast(0)
        val end = range?.let { it.last + 1 } ?: selectionEnd.coerceAtLeast(0)
        val inserted = "${mention.name} "

        pendingStyle = RichStyle()
        pendingAt = start
        editable.replace(start, end, inserted)

        isApplying = true
        editable.setSpan(
            MentionSpan(mention.href),
            start,
            start + mention.name.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        isApplying = false

        pendingStyle = null
        setSelection(start + inserted.length)
        requestFocus()
        publish()
    }

    fun removeMention() {
        val editable = text ?: return
        val cursor = selectionStart.coerceAtLeast(0)
        val span = editable.spanAt<MentionSpan>(cursor)
            ?: editable.spanAt<MentionSpan>((cursor - 1).coerceAtLeast(0))
            ?: return

        editable.removeSpan(span)
        publish()
    }

    override fun onSelectionChanged(
        selStart: Int,
        selEnd: Int,
    ) {
        super.onSelectionChanged(selStart, selEnd)
        if (!isReady || isTextChanging) return
        if (selStart != pendingAt) pendingStyle = null
        publish()
    }

    override fun onFocusChanged(
        focused: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?,
    ) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!isReady) return
        text?.let { editable ->
            editable.getSpans(0, editable.length, SpoilerSpan::class.java).forEach { span ->
                val start = editable.getSpanStart(span)
                val end = editable.getSpanEnd(span)
                span.isBlurred = !focused
                editable.removeSpan(span)
                editable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        publish()
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        val plainId = if (id == android.R.id.paste) android.R.id.pasteAsPlainText else id
        return super.onTextContextMenuItem(plainId)
    }

    private fun applyEdit(
        editable: Editable,
        edit: PendingEdit,
    ) {
        val insertedLines = edit.inserted.count { it == '\n' }
        val removedLines = edit.removed.count { it == '\n' }
        val lineType = edit.typesBefore.getOrElse(edit.line) { Paragraph }
        val touchesPending = pendingStyle != null && pendingAt in edit.start..(edit.start + edit.removed.length)
        if (touchesPending) pendingAt = edit.start + edit.inserted.length

        blockTypes = edit.typesBefore.take(edit.line) +
            List(insertedLines + 1) { lineType } +
            edit.typesBefore.drop(edit.line + removedLines + 1)

        when {
            edit.isEnter && lineType != Paragraph && edit.lineWasBlank -> {
                exitBlock(editable, edit)
            }
            edit.isBackspaceOverNewline && edit.typesBefore.getOrElse(edit.line + 1) { Paragraph } != Paragraph -> {
                liftBlock(editable, edit)
            }

            else -> {
                styleInserted(editable, edit, pendingStyle.takeIf { touchesPending })
                applyShortcuts(editable, edit)
            }
        }

        editable.renderBlocks()
    }

    private fun exitBlock(
        editable: Editable,
        edit: PendingEdit,
    ) {
        editable.delete(edit.start, edit.start + 1)
        blockTypes = edit.typesBefore.mapIndexed { index, type -> if (index == edit.line) Paragraph else type }
    }

    private fun liftBlock(
        editable: Editable,
        edit: PendingEdit,
    ) {
        editable.insert(edit.start, "\n")
        blockTypes = edit.typesBefore.mapIndexed { index, type -> if (index == edit.line + 1) Paragraph else type }
        setSelection(edit.start + 1)
    }

    private fun styleInserted(
        editable: Editable,
        edit: PendingEdit,
        pending: RichStyle?,
    ) {
        if (edit.inserted.isEmpty()) return

        val start = edit.start
        val end = start + edit.inserted.length
        val style = pending
            ?: edit.removedStyle
            ?: inheritedStyle(editable, before = start - 1, after = end)

        RichInlineStyle.entries.forEach { editable.setInline(start, end, it, style.has(it)) }
    }

    private fun applyShortcuts(
        editable: Editable,
        edit: PendingEdit,
    ) {
        val cursor = edit.start + edit.inserted.length
        val line = editable.lineOf(edit.start)
        val lineStart = editable.lineStarts()[line]
        val beforeCursor = editable.subSequence(lineStart, cursor).toString()

        if (edit.inserted == " " && blockTypes.getOrNull(line) == Paragraph) {
            val shortcut = when (beforeCursor) {
                "- ", "* " -> Bullet
                "> " -> Quote
                else -> null
            }
            if (shortcut != null) {
                editable.delete(lineStart, cursor)
                blockTypes = blockTypes.mapIndexed { index, type -> if (index == line) shortcut else type }
                return
            }
        }

        if (edit.inserted != "*") return

        val bold = boldShortcut.find(beforeCursor)
        val italic = italicShortcut.find(beforeCursor)
        val (match, style, markerLength) = when {
            bold != null -> Triple(bold, RichInlineStyle.Bold, 2)
            italic != null -> Triple(italic, RichInlineStyle.Italic, 1)
            else -> return
        }

        val matchStart = lineStart + match.range.first
        val matchEnd = lineStart + match.range.last + 1
        val contentEnd = matchEnd - markerLength * 2
        editable.delete(matchEnd - markerLength, matchEnd)
        editable.delete(matchStart, matchStart + markerLength)
        editable.setInline(matchStart, contentEnd, style, true)

        pendingStyle = inheritedStyle(editable, before = contentEnd - 1, after = contentEnd).without(style)
        pendingAt = contentEnd
    }

    private fun inheritedStyle(
        editable: Spanned,
        before: Int,
        after: Int,
    ): RichStyle {
        if (before < 0 || editable[before] == '\n') return RichStyle()

        val beforeStyle = editable.styleAt(before)
        val afterStyle = editable.takeIf { after < it.length }?.styleAt(after)
        return RichStyle(
            bold = beforeStyle.bold,
            italic = beforeStyle.italic,
            spoiler = beforeStyle.spoiler && afterStyle?.spoiler == true,
            href = beforeStyle.href?.takeIf { it == afterStyle?.href },
        )
    }

    private fun typingStyleAt(
        editable: Spanned,
        index: Int,
    ): RichStyle {
        return pendingStyle?.takeIf { pendingAt == index }
            ?: inheritedStyle(editable, before = index - 1, after = index)
    }

    private fun Editable.setRunStyle(
        style: RichStyle,
        start: Int,
        end: Int,
    ) {
        if (style.bold) setInline(start, end, RichInlineStyle.Bold, isApplied = true)
        if (style.italic) setInline(start, end, RichInlineStyle.Italic, isApplied = true)
        if (style.spoiler) setInline(start, end, RichInlineStyle.Spoiler, isApplied = true)
        style.href?.let { setSpan(MentionSpan(it), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }

    private fun Editable.setInline(
        start: Int,
        end: Int,
        style: RichInlineStyle,
        isApplied: Boolean,
    ) {
        if (start >= end) return

        when (style) {
            RichInlineStyle.Bold -> setStyleSpan(start, end, Typeface.BOLD, isApplied)
            RichInlineStyle.Italic -> setStyleSpan(start, end, Typeface.ITALIC, isApplied)
            RichInlineStyle.Spoiler -> setSpoilerSpan(start, end, isApplied)
        }
    }

    private fun Editable.setStyleSpan(
        start: Int,
        end: Int,
        typeface: Int,
        isApplied: Boolean,
    ) {
        clearRange(
            start = start,
            end = end,
            spans = getSpans(start, end, StyleSpan::class.java).filter { it.style == typeface },
            copy = { StyleSpan(typeface) },
        )
        if (isApplied) setSpan(StyleSpan(typeface), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        mergeSpans(getSpans(0, length, StyleSpan::class.java).filter { it.style == typeface }) {
            StyleSpan(typeface)
        }
    }

    private fun Editable.setSpoilerSpan(
        start: Int,
        end: Int,
        isApplied: Boolean,
    ) {
        val decorations = decorations ?: return
        val create = { SpoilerSpan(decorations.spoilerTint, decorations.blurRadius, isBlurred = !isFocused) }

        clearRange(start, end, getSpans(start, end, SpoilerSpan::class.java).toList(), create)
        if (isApplied) setSpan(create(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        mergeSpans(getSpans(0, length, SpoilerSpan::class.java).toList(), create)
    }

    private fun <T : Any> Editable.clearRange(
        start: Int,
        end: Int,
        spans: List<T>,
        copy: () -> T,
    ) {
        spans.forEach { span ->
            val spanStart = getSpanStart(span)
            val spanEnd = getSpanEnd(span)
            if (spanEnd <= start || spanStart >= end) return@forEach

            removeSpan(span)
            if (spanStart < start) setSpan(copy(), spanStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (spanEnd > end) setSpan(copy(), end, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun <T : Any> Editable.mergeSpans(
        spans: List<T>,
        copy: () -> T,
    ) {
        val ranges = spans
            .map { getSpanStart(it) until getSpanEnd(it) }
            .filter { !it.isEmpty() }
            .sortedBy { it.first }
            .fold(emptyList<IntRange>()) { merged, range ->
                val last = merged.lastOrNull()
                when {
                    last != null && range.first <= last.last + 1 -> {
                        merged.dropLast(1) + listOf(last.first..maxOf(last.last, range.last))
                    }

                    else -> {
                        merged + listOf(range)
                    }
                }
            }

        spans.forEach { removeSpan(it) }
        ranges.forEach { setSpan(copy(), it.first, it.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }

    private fun Editable.renderBlocks() {
        getSpans(0, length, BulletSpan::class.java).forEach { removeSpan(it) }
        getSpans(0, length, QuoteSpan::class.java).forEach { removeSpan(it) }

        val decorations = decorations ?: return
        val starts = lineStarts()
        blockTypes = List(starts.size) { blockTypes.getOrElse(it) { Paragraph } }

        starts.forEachIndexed { line, start ->
            val end = starts.getOrNull(line + 1) ?: length
            val span = when (blockTypes[line]) {
                Paragraph -> return@forEachIndexed
                Bullet -> BulletSpan(decorations.bulletGap, decorations.bulletColor, decorations.bulletRadius)
                Quote -> QuoteSpan(decorations.quoteColor, decorations.quoteStripe, decorations.quoteGap)
            }
            setSpan(span, start, end, Spannable.SPAN_PARAGRAPH)
        }
    }

    private fun publish() {
        if (!isReady || isApplying) return
        val editable = text ?: return

        onSnapshot(
            RichTextSnapshot(
                text = editable.toString(),
                markdown = editable.toRichLines(blockTypes).toMarkdown(),
                toolbar = toolbarState(editable),
                mentionQuery = mentionQuery(editable),
                isFocused = isFocused,
            ),
        )
    }

    private fun toolbarState(editable: Spanned): RichToolbarState {
        val start = selectionStart.coerceAtLeast(0)
        val end = selectionEnd.coerceAtLeast(0)
        val lines = editable.lineOf(start)..editable.lineOf(end)
        val blocks = lines.map { blockTypes.getOrElse(it) { Paragraph } }

        val style = when (start) {
            end -> typingStyleAt(editable, start)
            else -> (start until end)
                .map { editable.styleAt(it) }
                .reduce { shared, next ->
                    RichStyle(
                        bold = shared.bold && next.bold,
                        italic = shared.italic && next.italic,
                        spoiler = shared.spoiler && next.spoiler,
                        href = shared.href?.takeIf { it == next.href },
                    )
                }
        }

        return RichToolbarState(
            bold = style.bold,
            italic = style.italic,
            spoiler = style.spoiler,
            mention = style.href != null ||
                (start == end && start > 0 && editable.spanAt<MentionSpan>(start - 1) != null),
            bulletList = blocks.all { it == Bullet },
            quote = blocks.all { it == Quote },
        )
    }

    private fun mentionQuery(editable: Spanned): MentionQuery? {
        val cursor = selectionStart
        if (cursor < 0 || cursor != selectionEnd || !isFocused) return null

        val lineStart = editable.lineStarts()[editable.lineOf(cursor)]
        val beforeCursor = editable.subSequence(lineStart, cursor).toString()
        val at = beforeCursor.lastIndexOf('@').takeIf { it >= 0 } ?: return null
        val query = beforeCursor.substring(at + 1)

        return when {
            at > 0 && !beforeCursor[at - 1].isWhitespace() -> null
            query.length > MAX_MENTION_QUERY_LENGTH -> null
            query.firstOrNull()?.isWhitespace() == true -> null
            editable.spanAt<MentionSpan>(lineStart + at) != null -> null
            else -> MentionQuery(range = (lineStart + at) until cursor, query = query)
        }
    }

    private data class PendingEdit(
        val start: Int = 0,
        val line: Int = 0,
        val removed: String = "",
        val inserted: String = "",
        val removedStyle: RichStyle? = null,
        val typesBefore: List<RichBlockType> = listOf(Paragraph),
        val lineWasBlank: Boolean = false,
    ) {
        val isEnter: Boolean get() = inserted == "\n" && removed.isEmpty()
        val isBackspaceOverNewline: Boolean get() = removed == "\n" && inserted.isEmpty()
    }
}

private fun RichStyle.has(style: RichInlineStyle): Boolean {
    return when (style) {
        RichInlineStyle.Bold -> bold
        RichInlineStyle.Italic -> italic
        RichInlineStyle.Spoiler -> spoiler
    }
}

private fun RichStyle.without(style: RichInlineStyle): RichStyle {
    return when (style) {
        RichInlineStyle.Bold -> copy(bold = false)
        RichInlineStyle.Italic -> copy(italic = false)
        RichInlineStyle.Spoiler -> copy(spoiler = false)
    }
}

private fun RichStyle.toggle(style: RichInlineStyle): RichStyle {
    return when (style) {
        RichInlineStyle.Bold -> copy(bold = !bold)
        RichInlineStyle.Italic -> copy(italic = !italic)
        RichInlineStyle.Spoiler -> copy(spoiler = !spoiler)
    }
}

private fun CharSequence.lineOf(index: Int): Int {
    return subSequence(0, index.coerceIn(0, length)).count { it == '\n' }
}

private fun CharSequence.lineTextAround(index: Int): String {
    val safeIndex = index.coerceIn(0, length)
    val start = subSequence(0, safeIndex).lastIndexOf('\n') + 1
    val end = indexOf('\n', safeIndex).takeIf { it >= 0 } ?: length
    return subSequence(start, end).toString()
}
