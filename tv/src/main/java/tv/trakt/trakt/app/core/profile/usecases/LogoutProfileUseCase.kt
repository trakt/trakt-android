package tv.trakt.trakt.app.core.profile.usecases

import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics

internal class LogoutProfileUseCase(
    private val apiClients: Array<ApiClient>,
    private val sessionManager: SessionManager,
    private val showsSyncLocalDataSource: ShowsSyncLocalDataSource,
    private val moviesSyncLocalDataSource: ShowsSyncLocalDataSource,
    private val episodesSyncLocalDataSource: EpisodesSyncLocalDataSource,
    private val analytics: Analytics,
) {
    suspend fun logoutUser() {
        sessionManager.clear()
        analytics.setUserId(null)

        apiClients
            .forEach { api ->
                api.client.authProvider<BearerAuthProvider>()?.clearToken()
            }

        showsSyncLocalDataSource.clear()
        moviesSyncLocalDataSource.clear()
        episodesSyncLocalDataSource.clear()
    }
}
