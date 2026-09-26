package tv.trakt.trakt.core.main.usecases

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.api.imdb.model.ImdbLookupEpisodeDto
import tv.trakt.trakt.common.networking.api.imdb.model.ImdbLookupIdsDto
import tv.trakt.trakt.common.networking.api.imdb.model.ImdbLookupItemDto
import tv.trakt.trakt.common.networking.api.imdb.model.ImdbLookupMediaDto
import tv.trakt.trakt.core.main.model.ImdbLinkTarget

class MapToImdbLinkTargetTest {
    private fun media(id: Int) = ImdbLookupMediaDto(ids = ImdbLookupIdsDto(trakt = id))

    @Test
    fun `maps movies`() {
        assertEquals(
            ImdbLinkTarget.Movie(234.toTraktId()),
            mapToImdbLinkTarget(ImdbLookupItemDto(type = "movie", movie = media(234))),
        )
    }

    @Test
    fun `maps shows`() {
        assertEquals(
            ImdbLinkTarget.Show(1388.toTraktId()),
            mapToImdbLinkTarget(ImdbLookupItemDto(type = "show", show = media(1388))),
        )
    }

    @Test
    fun `maps episodes to their show`() {
        val item = ImdbLookupItemDto(
            type = "episode",
            show = media(1388),
            episode = ImdbLookupEpisodeDto(
                season = 1,
                number = 1,
                ids = ImdbLookupIdsDto(trakt = 73482),
            ),
        )

        assertEquals(
            ImdbLinkTarget.Episode(
                showId = 1388.toTraktId(),
                episodeId = 73482.toTraktId(),
                season = 1,
                number = 1,
            ),
            mapToImdbLinkTarget(item),
        )
    }

    @Test
    fun `maps people`() {
        assertEquals(
            ImdbLinkTarget.Person(13437.toTraktId()),
            mapToImdbLinkTarget(ImdbLookupItemDto(type = "person", person = media(13437))),
        )
    }

    @Test
    fun `skips unknown items`() {
        assertNull(mapToImdbLinkTarget(ImdbLookupItemDto(type = "list")))
    }
}
