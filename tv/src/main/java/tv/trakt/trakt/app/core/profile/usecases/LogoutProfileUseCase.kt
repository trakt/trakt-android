package tv.trakt.trakt.app.core.profile.usecases

import tv.trakt.trakt.app.core.search.data.local.RecentSearchLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.common.auth.session.SessionManager

internal class LogoutProfileUseCase(
    private val sessionManager: SessionManager,
    private val showsSyncLocalDataSource: ShowsSyncLocalDataSource,
    private val moviesSyncLocalDataSource: ShowsSyncLocalDataSource,
    private val episodesSyncLocalDataSource: EpisodesSyncLocalDataSource,
    private val recentSearchLocalDataSource: RecentSearchLocalDataSource,
) {
    suspend fun logoutUser() {
        sessionManager.clear()
        showsSyncLocalDataSource.clear()
        moviesSyncLocalDataSource.clear()
        episodesSyncLocalDataSource.clear()
        recentSearchLocalDataSource.clear()
    }
}
