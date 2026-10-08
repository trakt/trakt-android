package tv.trakt.trakt.core.summary.credits.model

import tv.trakt.trakt.common.model.TraktId

internal sealed interface CreditsSource {
    data class Movie(
        val movieId: TraktId,
    ) : CreditsSource

    data class Show(
        val showId: TraktId,
    ) : CreditsSource

    data class Episode(
        val showId: TraktId,
        val season: Int,
        val episode: Int,
    ) : CreditsSource
}
