package tv.trakt.trakt.core.trivia.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

internal enum class TriviaFilter(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    NoSpoilers(
        R.string.button_text_trivia_no_spoilers,
        R.drawable.ic_no_spoiler,
    ),
    Spoilers(
        R.string.button_text_trivia_spoilers,
        R.drawable.ic_spoiler,
    ),
}
