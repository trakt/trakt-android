package tv.trakt.trakt.core.ratings.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RatingDelightTest {
    @Test
    fun `perfect 10 throws popcorn`() {
        assertEquals(RatingDelight.Popcorn, ratingDelight(10))
    }

    @Test
    fun `one star or less throws a rotten tomato`() {
        assertEquals(RatingDelight.RottenTomato, ratingDelight(1))
        assertEquals(RatingDelight.RottenTomato, ratingDelight(2))
    }

    @Test
    fun `ratings in between stay quiet`() {
        listOf(3, 5, 7, 9).forEach { assertNull(ratingDelight(it)) }
    }

    @Test
    fun `cleared rating stays quiet`() {
        assertNull(ratingDelight(0))
    }
}
