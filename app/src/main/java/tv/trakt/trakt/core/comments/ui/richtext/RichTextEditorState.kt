package tv.trakt.trakt.core.comments.ui.richtext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import tv.trakt.trakt.core.comments.model.CommentMention

@Stable
internal class RichTextEditorState {
    private var view: RichTextEditText? = null
    private var pending: ((RichTextEditText) -> Unit)? = null

    private var snapshot by mutableStateOf(
        RichTextSnapshot(
            text = "",
            markdown = "",
            toolbar = RichToolbarState(),
            mentionQuery = null,
            isFocused = false,
        ),
    )

    val text: String get() = snapshot.text
    val markdown: String get() = snapshot.markdown
    val toolbar: RichToolbarState get() = snapshot.toolbar
    val mentionQuery: MentionQuery? get() = snapshot.mentionQuery
    val isFocused: Boolean get() = snapshot.isFocused

    fun setText(value: String) {
        withView { it.setPlainText(value) }
    }

    fun setMarkdown(value: String) {
        withView { it.setRichLines(value.parseMarkdown()) }
    }

    fun toggleInline(style: RichInlineStyle) {
        view?.toggleInline(style)
    }

    fun toggleBlock(type: RichBlockType) {
        view?.toggleBlock(type)
    }

    fun insertMention(
        mention: CommentMention,
        range: IntRange? = null,
    ) {
        view?.insertMention(mention, range)
    }

    fun removeMention() {
        view?.removeMention()
    }

    fun focus() {
        view?.requestFocus()
    }

    internal fun attach(editText: RichTextEditText) {
        view = editText
        editText.onSnapshot = { snapshot = it }
        pending?.invoke(editText)
        pending = null
    }

    internal fun detach(editText: RichTextEditText) {
        if (view != editText) return
        editText.onSnapshot = {}
        view = null
    }

    private fun withView(action: (RichTextEditText) -> Unit) {
        val attached = view ?: run {
            pending = action
            return
        }
        action(attached)
    }
}

@Composable
internal fun rememberRichTextEditorState(): RichTextEditorState {
    return remember { RichTextEditorState() }
}
