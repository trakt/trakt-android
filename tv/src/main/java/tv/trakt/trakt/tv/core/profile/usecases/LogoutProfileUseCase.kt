package tv.trakt.trakt.tv.core.profile.usecases

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.tv.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.tv.core.sync.data.local.shows.ShowsSyncLocalDataSource

internal class LogoutProfileUseCase(
    private val sessionManager: SessionManager,
    private val showsSyncLocalDataSource: ShowsSyncLocalDataSource,
    private val moviesSyncLocalDataSource: ShowsSyncLocalDataSource,
    private val episodesSyncLocalDataSource: EpisodesSyncLocalDataSource,
) {
    suspend fun logoutUser() {
        sessionManager.clear()
        showsSyncLocalDataSource.clear()
        moviesSyncLocalDataSource.clear()
        episodesSyncLocalDataSource.clear()
    }
}
