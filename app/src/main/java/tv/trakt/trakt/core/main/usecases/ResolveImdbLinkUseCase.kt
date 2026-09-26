package tv.trakt.trakt.core.main.usecases

import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.api.imdb.ImdbLookupApi
import tv.trakt.trakt.common.networking.api.imdb.model.ImdbLookupItemDto
import tv.trakt.trakt.core.main.model.ImdbLink
import tv.trakt.trakt.core.main.model.ImdbLinkTarget

internal fun mapToImdbLinkTarget(item: ImdbLookupItemDto): ImdbLinkTarget? {
    val movie = item.movie
    val show = item.show
    val episode = item.episode
    val person = item.person
    return when {
        movie != null -> ImdbLinkTarget.Movie(movie.ids.trakt.toTraktId())
        episode != null && show != null -> ImdbLinkTarget.Episode(
            showId = show.ids.trakt.toTraktId(),
            episodeId = episode.ids.trakt.toTraktId(),
            season = episode.season,
            number = episode.number,
        )
        show != null -> ImdbLinkTarget.Show(show.ids.trakt.toTraktId())
        person != null -> ImdbLinkTarget.Person(person.ids.trakt.toTraktId())
        else -> null
    }
}

internal class ResolveImdbLinkUseCase(
    private val imdbLookupApi: ImdbLookupApi,
) {
    suspend fun resolve(link: ImdbLink): ImdbLinkTarget {
        return imdbLookupApi
            .getByImdbId(link.imdbId)
            .firstNotNullOfOrNull(::mapToImdbLinkTarget)
            ?: ImdbLinkTarget.NotFound
    }
}
