package tv.trakt.trakt.core.trivia.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

internal enum class TriviaFilter(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    NoSpoilers(
        R.string.text_trivia_filter_no_spoilers,
        R.drawable.ic_no_spoiler,
    ),
    Spoilers(
        R.string.text_trivia_filter_spoilers,
        R.drawable.ic_spoiler,
    ),
}
