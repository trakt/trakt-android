package tv.trakt.trakt.core.summary.people.helpers

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem

internal fun mergePersonCredits(
    showCredits: Map<String, List<PersonCreditItem.ShowItem>>?,
    movieCredits: Map<String, List<PersonCreditItem.MovieItem>>?,
    mode: MediaMode,
): ImmutableMap<String, ImmutableList<PersonCreditItem>> {
    val shows = showCredits.takeIf { mode.isMediaOrShows }.orEmpty()
    val movies = movieCredits.takeIf { mode.isMediaOrMovies }.orEmpty()

    return (shows.keys + movies.keys)
        .associateWith { position ->
            (shows[position].orEmpty() + movies[position].orEmpty())
                .distinctBy { it.key }
                .sortedByDescending { it.released }
                .toImmutableList()
        }
        .toImmutableMap()
}

internal fun historyPersonCredits(
    showCredits: Map<String, List<PersonCreditItem.ShowItem>>?,
    movieCredits: Map<String, List<PersonCreditItem.MovieItem>>?,
    collection: UserCollectionState,
    mode: MediaMode = MediaMode.Media,
): ImmutableList<PersonCreditItem> {
    val shows = showCredits.takeIf { mode.isMediaOrShows }?.values?.flatten().orEmpty()
    val movies = movieCredits.takeIf { mode.isMediaOrMovies }?.values?.flatten().orEmpty()

    return (shows + movies)
        .filter { collection.isWatched(it.id, it.type, it.airedEpisodes) }
        .distinctBy { it.key }
        .sortedByDescending { it.released }
        .toImmutableList()
}
