package tv.trakt.trakt.app.core.search.usecase.recents

import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.core.search.data.local.RecentSearchLocalDataSource
import tv.trakt.trakt.app.core.shows.model.Show

internal class AddRecentSearchUseCase(
    private val recentsLocalSource: RecentSearchLocalDataSource,
) {
    suspend fun addRecentSearchShow(show: Show) {
        recentsLocalSource.addShow(show)
    }

    suspend fun addRecentSearchMovie(movie: Movie) {
        recentsLocalSource.addMovie(movie)
    }
}
