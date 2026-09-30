package tv.trakt.trakt.core.applinks.usecases

import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.api.imdb.ImdbLookupApi
import tv.trakt.trakt.common.networking.api.imdb.model.ImdbLookupItemDto
import tv.trakt.trakt.core.applinks.AppLinkEvent

internal fun mapToAppLinkEvent(item: ImdbLookupItemDto): AppLinkEvent? {
    return when (item.type) {
        "movie" -> {
            item.movie?.let { AppLinkEvent.OpenMovie(it.ids.trakt.toTraktId()) }
        }
        "show" -> {
            item.show?.let { AppLinkEvent.OpenShow(it.ids.trakt.toTraktId()) }
        }
        "person" -> {
            item.person?.let { AppLinkEvent.OpenPerson(it.ids.trakt.toTraktId()) }
        }
        "episode" -> {
            val show = item.show ?: return null
            val episode = item.episode ?: return null
            AppLinkEvent.OpenEpisode(
                showId = show.ids.trakt.toTraktId(),
                episodeId = episode.ids.trakt.toTraktId(),
                season = episode.season,
                number = episode.number,
            )
        }
        else -> {
            null
        }
    }
}

internal class ResolveImdbLinkUseCase(
    private val imdbLookupApi: ImdbLookupApi,
) {
    suspend fun resolve(imdbId: String): AppLinkEvent {
        return imdbLookupApi
            .getByImdbId(imdbId)
            .firstNotNullOfOrNull(::mapToAppLinkEvent)
            ?: AppLinkEvent.NotFound
    }
}
