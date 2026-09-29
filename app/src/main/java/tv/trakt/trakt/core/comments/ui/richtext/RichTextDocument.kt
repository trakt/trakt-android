package tv.trakt.trakt.core.comments.ui.richtext

internal enum class RichBlockType {
    Paragraph,
    Bullet,
    Quote,
}

internal data class RichStyle(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val spoiler: Boolean = false,
    val href: String? = null,
)

internal data class RichRun(
    val text: String,
    val style: RichStyle = RichStyle(),
)

internal data class RichLine(
    val type: RichBlockType,
    val runs: List<RichRun>,
) {
    val text: String get() = runs.joinToString("") { it.text }
}
