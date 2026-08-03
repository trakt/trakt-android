package tv.trakt.trakt.app.core.lists.filters

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Availability
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Certification
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Region
import java.time.Year

class TvListFilterConfigurationTest {
    @Test
    fun `fixed movie configuration normalizes media and visibility`() {
        val filter = GlobalFilter.Default.copy(
            mode = MediaMode.Shows,
            subgenre = persistentListOf("unused"),
            hideWatched = true,
            hideWatchlist = true,
        )

        val normalized = TvListFilterConfiguration.MoviesWatchlist.normalize(filter)

        assertEquals(MediaMode.Movies, normalized.mode)
        assertTrue(normalized.hideWatched)
        assertFalse(normalized.hideWatchlist)
        assertNull(normalized.subgenre)
    }

    @Test
    fun `mixed configuration keeps every supported media mode`() {
        MediaMode.entries.forEach { mode ->
            val normalized = TvListFilterConfiguration.MixedList.normalize(
                GlobalFilter.Default.copy(mode = mode),
            )

            assertEquals(mode, normalized.mode)
        }
    }

    @Test
    fun `reset clears selections and preserves applicable media mode`() {
        val filter = GlobalFilter(
            mode = MediaMode.Shows,
            genre = persistentListOf(MediaGenre.Drama),
            years = 1990 to 1999,
            hideWatched = true,
            hideWatchlist = true,
        )

        val reset = TvListFilterConfiguration.MixedList.reset(filter)

        assertEquals(MediaMode.Shows, reset.mode)
        assertFalse(reset.isActive)
    }

    @Test
    fun `advanced conversion keeps compatible values and clears incompatible values`() {
        val filter = GlobalFilter(
            mode = MediaMode.Media,
            genre = persistentListOf(MediaGenre.Drama, MediaGenre.Comedy),
            years = 1995 to 2004,
            runtime = 17 to 83,
            availability = persistentListOf(
                Availability.StreamingNow,
                Availability.MyFavorites,
            ),
            certification = persistentListOf(
                Certification.Teens,
                Certification.Mature,
            ),
            region = Region.Europe,
            countries = persistentListOf("se", "no"),
            rating = 37 to 88,
            hideWatched = true,
            hideWatchlist = true,
        )

        val converted = TvListFilterConfiguration.MixedList.toSimple(filter)

        assertTrue(TvListFilterConfiguration.MixedList.hasSimpleIncompatibleValues(filter))
        assertNull(converted.genre)
        assertNull(converted.availability)
        assertNull(converted.certification)
        assertNull(converted.years)
        assertNull(converted.runtime)
        assertNull(converted.countries)
        assertNull(converted.rating)
        assertEquals(Region.Europe, converted.region)
        assertTrue(converted.hideWatched)
        assertTrue(converted.hideWatchlist)
    }

    @Test
    fun `simple-compatible values do not require conversion confirmation`() {
        val filter = GlobalFilter(
            mode = MediaMode.Movies,
            genre = persistentListOf(MediaGenre.Drama),
            years = 1990 to 1999,
            runtime = 91 to 120,
            availability = persistentListOf(Availability.StreamingNow),
            certification = persistentListOf(Certification.Teens),
            region = Region.Europe,
            rating = 35 to 85,
            hideWatched = true,
            hideWatchlist = true,
        )

        assertFalse(TvListFilterConfiguration.MixedList.hasSimpleIncompatibleValues(filter))
        assertEquals(filter, TvListFilterConfiguration.MixedList.toSimple(filter))
    }

    @Test
    fun `exact current year converts to the simple current-year option`() {
        val currentYear = Year.now().value
        val filter = GlobalFilter.Default.copy(
            years = currentYear to currentYear,
        )

        val converted = TvListFilterConfiguration.MixedList.toSimple(filter)

        assertFalse(TvListFilterConfiguration.MixedList.hasSimpleIncompatibleValues(filter))
        assertEquals(0 to 0, converted.years)
    }

    @Test
    fun `watchlist and list configurations expose contextual visibility`() {
        assertTrue(TvListFilterConfiguration.MoviesWatchlist.showHideWatched)
        assertFalse(TvListFilterConfiguration.MoviesWatchlist.showHideWatchlisted)
        assertTrue(TvListFilterConfiguration.ShowsWatchlist.showHideWatched)
        assertFalse(TvListFilterConfiguration.ShowsWatchlist.showHideWatchlisted)
        assertTrue(TvListFilterConfiguration.MixedList.showHideWatched)
        assertTrue(TvListFilterConfiguration.MixedList.showHideWatchlisted)
    }
}
