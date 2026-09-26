package tv.trakt.trakt.core.main.model

import tv.trakt.trakt.common.model.TraktId

internal sealed interface ImdbLinkTarget {
    data class Movie(
        val movieId: TraktId,
    ) : ImdbLinkTarget

    data class Show(
        val showId: TraktId,
    ) : ImdbLinkTarget

    data class Episode(
        val showId: TraktId,
        val episodeId: TraktId,
        val season: Int,
        val number: Int,
    ) : ImdbLinkTarget

    data class Person(
        val personId: TraktId,
    ) : ImdbLinkTarget

    data object NotFound : ImdbLinkTarget

    data object Failed : ImdbLinkTarget
}
