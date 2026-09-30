package tv.trakt.trakt.core.applinks.usecases

import org.openapitools.client.models.GetSearchQuery200ResponseInner.Type
import tv.trakt.trakt.common.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.SearchItemDto
import tv.trakt.trakt.core.applinks.AppLinkEvent

internal fun mapToAppLinkEvent(item: SearchItemDto): AppLinkEvent? {
    return when (item.type) {
        Type.MOVIE -> {
            item.movie?.let { AppLinkEvent.OpenMovie(it.ids.trakt.toTraktId()) }
        }
        Type.SHOW -> {
            item.show?.let { AppLinkEvent.OpenShow(it.ids.trakt.toTraktId()) }
        }
        Type.PERSON -> {
            item.person?.let { AppLinkEvent.OpenPerson(it.ids.trakt.toTraktId()) }
        }
        Type.EPISODE -> {
            val show = item.show ?: return null
            val episode = item.episode ?: return null
            AppLinkEvent.OpenEpisode(
                showId = show.ids.trakt.toTraktId(),
                episodeId = episode.ids.trakt.toTraktId(),
                season = episode.season,
                number = episode.number,
            )
        }
        Type.LIST -> {
            null
        }
    }
}

internal class ResolveImdbLinkUseCase(
    private val remoteSource: SearchRemoteDataSource,
) {
    suspend fun resolve(imdbId: String): AppLinkEvent {
        return remoteSource
            .getImdbLookup(imdbId)
            .firstNotNullOfOrNull(::mapToAppLinkEvent)
            ?: AppLinkEvent.NotFound
    }
}
