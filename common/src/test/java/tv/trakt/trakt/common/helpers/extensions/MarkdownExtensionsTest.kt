package tv.trakt.trakt.common.helpers.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownExtensionsTest {
    private val link = Color.Blue
    private val mention = Color.Red

    @Test
    fun `bold and italic markers are removed and styled`() {
        val result = "a **bold** and *italic* and _also_".toMarkdownText(link)

        assertEquals("a bold and italic and also", result.text)
        assertTrue(result.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        assertEquals(2, result.spanStyles.count { it.item.fontStyle == FontStyle.Italic })
    }

    @Test
    fun `strikethrough and code are styled`() {
        val result = "~~gone~~ `code`".toMarkdownText(link)

        assertEquals("gone code", result.text)
        assertTrue(result.spanStyles.any { it.item.textDecoration == TextDecoration.LineThrough })
    }

    @Test
    fun `markdown link keeps label and url`() {
        val result = "see [Trakt](https://trakt.tv) now".toMarkdownText(link)

        assertEquals("see Trakt now", result.text)
        val annotation = result.getLinkAnnotations(0, result.length).single().item as LinkAnnotation.Url
        assertEquals("https://trakt.tv", annotation.url)
    }

    @Test
    fun `bare url becomes a link without trailing punctuation`() {
        val result = "go to https://trakt.tv/movies.".toMarkdownText(link)

        val annotation = result.getLinkAnnotations(0, result.length).single().item as LinkAnnotation.Url
        assertEquals("https://trakt.tv/movies", annotation.url)
    }

    @Test
    fun `non http links are left as text`() {
        val result = "[x](javascript:alert(1))".toMarkdownText(link)

        assertTrue(result.getLinkAnnotations(0, result.length).isEmpty())
    }

    @Test
    fun `lists headings and quotes are converted per line`() {
        val result = "# Title\n- one\n* two\n1. three\n> quoted".toMarkdownText(link)

        assertEquals("Title\n•  one\n•  two\n1.  three\nquoted", result.text)
    }

    @Test
    fun `escaped markers stay literal`() {
        assertEquals("*not italic*", "\\*not italic\\*".toMarkdownText(link).text)
    }

    @Test
    fun `snake_case words and math stars are untouched`() {
        assertEquals("my_var_name and 2 * 3 * 4", "my_var_name and 2 * 3 * 4".toMarkdownText(link).text)
    }

    @Test
    fun `mentions are highlighted only when a color is given`() {
        val plain = "hi @vlad".toMarkdownText(link)
        val highlighted = "hi @vlad".toMarkdownText(link, mention)

        assertTrue(plain.spanStyles.isEmpty())
        assertTrue(highlighted.spanStyles.any { it.item.color == mention })
    }
}
