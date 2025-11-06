package tv.trakt.trakt.common.helpers

import tv.trakt.trakt.common.helpers.extensions.nowLocal

/**
 * Having fun with movie quotes to greet the user!
 */
class GreetingQuotes {
    companion object {
        fun getTodayQuote(): String {
            val today = nowLocal()
            return when (today.hour) {
                in 6..11 -> "Good morning."
                in 12..18 -> "Good afternoon."
                in 19..23 -> "Good evening."
                else -> "Hello!"
            }
        }
    }
}
