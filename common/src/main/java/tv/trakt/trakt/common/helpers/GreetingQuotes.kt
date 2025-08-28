package tv.trakt.trakt.common.helpers

import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import kotlin.random.Random

/**
 * Having fun with movie quotes to greet the user!
 */
class GreetingQuotes {
    companion object {
        private val movieQuotes = listOf(
            "Hello there!", // Star Wars
            "Good morning, Vietnam!", // Good Morning, Vietnam
            "You had me at hello.", // Jerry Maguire
            "Well, hello beautiful.", // The Mask
            "Hello, my name is Inigo Montoya.", // The Princess Bride
            "Good morning, sunshine!", // The Shining
            "Buongiorno, principessa!", // Life is Beautiful
            "Well, hello there, handsome.", // Pretty Woman
            "Rise and shine!", // Groundhog Day
            "Top of the morning to you!", // Irish-themed movies
            "Good day, mate!", // Crocodile Dundee
            "Hello, gorgeous.", // Funny Girl
            "Greetings, earthlings!", // Mars Attacks!
            "Good morning, starshine!", // Hair
            "Hello, old friend.", // X-Men series
            "Welcome to the party, pal!", // Die Hard
            "Good evening, ladies and gentlemen.", // The Greatest Showman
            "Morning, sunshine!", // Kill Bill
            "Hello, Clarice.", // The Silence of the Lambs
            "Good morning, class!", // Dead Poets Society
            "Hiya, pal!", // Goodfellas
            "Well, hello there, sailor!", // Various classic films
            "Good morning, America!", // Good Morning America
            "Hello, sweetie.", // Doctor Who movies
            "Rise and shine, sleeping beauty!", // Shrek
            "Good evening, Mr. Bond.", // James Bond films
            "Hello, world!", // The Social Network
            "Morning, boss!", // The Godfather
            "Well, well, well, hello there!", // Various comedies
            "Good morning, good morning!", // Singing in the Rain
            "Hello, is it me you're looking for?", // Various romantic comedies
        )

        fun getTodayQuote(): String {
            val today = ZonedDateTime.now(UTC)
            return getQuoteForDate(today.dayOfMonth, today.monthValue)
        }

        /**
         * Gets a greeting quote for a specific day and month.
         * @param dayOfMonth The day of the month (1-31)
         * @param month The month (1-12)
         */
        fun getQuoteForDate(
            dayOfMonth: Int,
            month: Int,
        ): String {
            // Create a seed that combines day and month to ensure different quotes per month
            // This ensures that day 15 in January will give a different quote than day 15 in February
            val seed = (month * 100) + dayOfMonth
            val random = Random(seed.toLong())

            return movieQuotes[random.nextInt(movieQuotes.size)]
        }
    }
}
