package tv.trakt.trakt.core.search.usecase.recents

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.search.data.local.RecentSearchLocalDataSource

internal class AddRecentSearchUseCase(
    private val sessionManager: SessionManager,
    private val recentsLocalSource: RecentSearchLocalDataSource,
) {
    suspend fun addRecentSearchShow(show: Show) {
        if (sessionManager.isAuthenticated()) {
            recentsLocalSource.addShow(show)
        }
    }

    suspend fun addRecentSearchMovie(movie: Movie) {
        if (sessionManager.isAuthenticated()) {
            recentsLocalSource.addMovie(movie)
        }
    }

    suspend fun addRecentSearchPerson(person: Person) {
        if (sessionManager.isAuthenticated()) {
            recentsLocalSource.addPerson(person)
        }
    }
}
