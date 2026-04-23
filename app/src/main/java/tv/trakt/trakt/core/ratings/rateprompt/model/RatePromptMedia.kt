package tv.trakt.trakt.core.ratings.rateprompt.model

import tv.trakt.trakt.common.model.Movie

data class RatePromptMedia(
    val movie: Movie,
    val favorite: Boolean,
)
