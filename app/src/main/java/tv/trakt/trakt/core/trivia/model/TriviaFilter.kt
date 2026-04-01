package tv.trakt.trakt.core.trivia.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

internal enum class TriviaFilter(
    @param:StringRes val displayRes: Int,
) {
    NoSpoilers(R.string.text_trivia_filter_no_spoilers),
    Spoilers(R.string.text_trivia_filter_spoilers),
}
