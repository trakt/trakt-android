package tv.trakt.trakt.app.core.people.helpers

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.people.model.PersonHistoryItem
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

internal fun buildHistoryCredits(
    shows: List<Show>?,
    movies: List<Movie>?,
    collection: UserCollectionState,
): ImmutableList<PersonHistoryItem> {
    val watchedShows = shows.orEmpty()
        .filter { collection.isWatched(it.ids.trakt, MediaType.Show, it.airedEpisodes) }
        .map { PersonHistoryItem.ShowItem(it) }

    val watchedMovies = movies.orEmpty()
        .filter { collection.isWatched(it.ids.trakt, MediaType.Movie, null) }
        .map { PersonHistoryItem.MovieItem(it) }

    return (watchedShows + watchedMovies)
        .distinctBy { it.key }
        .sortedByDescending { it.released }
        .toImmutableList()
}
