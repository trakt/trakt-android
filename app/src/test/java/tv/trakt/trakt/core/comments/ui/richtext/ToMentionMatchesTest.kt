package tv.trakt.trakt.core.comments.ui.richtext

import org.junit.Assert.assertEquals
import org.junit.Test
import tv.trakt.trakt.core.comments.model.CommentMention

class ToMentionMatchesTest {
    private val mentions = listOf(
        CommentMention(name = "Steve Carell", href = "https://app.trakt.tv/people/steve-carell"),
        CommentMention(name = "Steven Yeun", href = "https://app.trakt.tv/people/steven-yeun"),
        CommentMention(name = "Rainn Wilson", href = "https://app.trakt.tv/people/rainn-wilson"),
    )

    @Test
    fun `returns the first entries when the query is empty`() {
        val result = mentions.toMentionMatches(query = "  ", limit = 2)

        assertEquals(listOf("Steve Carell", "Steven Yeun"), result.map { it.name })
    }

    @Test
    fun `matches names regardless of case`() {
        val result = mentions.toMentionMatches(query = "WILS", limit = 5)

        assertEquals(listOf("Rainn Wilson"), result.map { it.name })
    }

    @Test
    fun `returns nothing when no name matches`() {
        assertEquals(emptyList<CommentMention>(), mentions.toMentionMatches(query = "zzz", limit = 5))
    }
}
