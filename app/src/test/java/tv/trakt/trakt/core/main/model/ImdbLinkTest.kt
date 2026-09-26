package tv.trakt.trakt.core.main.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImdbLinkTest {
    @Test
    fun `parses title links`() {
        assertEquals(
            ImdbLink.Title("tt0903747"),
            ImdbLink.parse("https://www.imdb.com/title/tt0903747/"),
        )
    }

    @Test
    fun `parses person links`() {
        assertEquals(
            ImdbLink.Person("nm0000151"),
            ImdbLink.parse("https://www.imdb.com/name/nm0000151/?ref_=nv_sr_srsg_0"),
        )
    }

    @Test
    fun `parses mobile, bare and localized hosts`() {
        val expected = ImdbLink.Title("tt0111161")
        assertEquals(expected, ImdbLink.parse("https://m.imdb.com/title/tt0111161"))
        assertEquals(expected, ImdbLink.parse("http://imdb.com/title/tt0111161/"))
        assertEquals(expected, ImdbLink.parse("https://www.imdb.com/de/title/tt0111161/"))
    }

    @Test
    fun `parses title sub pages`() {
        assertEquals(
            ImdbLink.Title("tt0903747"),
            ImdbLink.parse("https://www.imdb.com/title/tt0903747/episodes/?season=2"),
        )
    }

    @Test
    fun `rejects non title links`() {
        assertNull(ImdbLink.parse(null))
        assertNull(ImdbLink.parse("https://www.imdb.com/chart/top/"))
        assertNull(ImdbLink.parse("https://www.imdb.com/title/"))
        assertNull(ImdbLink.parse("https://www.imdb.com/title/tt0903747abc"))
        assertNull(ImdbLink.parse("https://notimdb.com/title/tt0903747/"))
        assertNull(ImdbLink.parse("https://trakt.tv/shows/breaking-bad"))
    }
}
