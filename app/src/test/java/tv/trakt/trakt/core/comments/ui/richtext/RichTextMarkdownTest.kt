package tv.trakt.trakt.core.comments.ui.richtext

import org.junit.Assert.assertEquals
import org.junit.Test
import tv.trakt.trakt.core.comments.ui.richtext.RichBlockType.Bullet
import tv.trakt.trakt.core.comments.ui.richtext.RichBlockType.Paragraph
import tv.trakt.trakt.core.comments.ui.richtext.RichBlockType.Quote

class RichTextMarkdownTest {
    private val bold = RichStyle(bold = true)
    private val italic = RichStyle(italic = true)
    private val spoiler = RichStyle(spoiler = true)

    private fun line(
        vararg runs: RichRun,
        type: RichBlockType = Paragraph,
    ) = RichLine(type, runs.toList())

    private fun plain(text: String) = RichRun(text)

    @Test
    fun `keeps plain text`() {
        assertEquals("Great movie", listOf(line(plain("Great movie"))).toMarkdown())
    }

    @Test
    fun `wraps bold and italic`() {
        val lines = listOf(
            line(plain("It was "), RichRun("great", bold), plain(" and "), RichRun("fun", italic)),
        )

        assertEquals("It was **great** and *fun*", lines.toMarkdown())
    }

    @Test
    fun `wraps spoilers around other styles`() {
        val lines = listOf(
            line(
                plain("He is "),
                RichRun("the", spoiler.copy(bold = true)),
                RichRun(" killer", spoiler),
            ),
        )

        assertEquals("He is [spoiler]**the** killer[/spoiler]", lines.toMarkdown())
    }

    @Test
    fun `writes mentions as links`() {
        val lines = listOf(
            line(
                plain("Loved "),
                RichRun("Steve Carell", RichStyle(href = "https://app.trakt.tv/people/steve-carell")),
                plain(" here"),
            ),
        )

        assertEquals("Loved [Steve Carell](https://app.trakt.tv/people/steve-carell) here", lines.toMarkdown())
    }

    @Test
    fun `moves edge spaces out of emphasis`() {
        val lines = listOf(line(plain("so"), RichRun(" good ", bold), plain("really")))

        assertEquals("so **good** really", lines.toMarkdown())
    }

    @Test
    fun `drops emphasis on whitespace only`() {
        assertEquals("a b", listOf(line(plain("a"), RichRun(" ", bold), plain("b"))).toMarkdown())
    }

    @Test
    fun `writes bullet lists and quotes`() {
        val lines = listOf(
            line(plain("one"), type = Bullet),
            line(plain("two"), type = Bullet),
            line(plain("so good"), type = Quote),
            line(plain("after")),
        )

        assertEquals("- one\n- two\n\n> so good\n\nafter", lines.toMarkdown())
    }

    @Test
    fun `keeps single and double line breaks`() {
        val lines = listOf(line(plain("one")), line(plain("two")), line(), line(plain("three")))

        assertEquals("one\ntwo\n\nthree", lines.toMarkdown())
    }

    @Test
    fun `escapes typed markdown characters`() {
        val lines = listOf(line(plain("snake_case *not bold* [x]")), line(plain("# not a heading")))

        assertEquals("snake\\_case \\*not bold\\* \\[x\\]\n\\# not a heading", lines.toMarkdown())
    }

    @Test
    fun `returns an empty string for an empty document`() {
        assertEquals("", listOf(line()).toMarkdown())
    }
}
