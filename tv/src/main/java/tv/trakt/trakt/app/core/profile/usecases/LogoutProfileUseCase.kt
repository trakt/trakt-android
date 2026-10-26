package tv.trakt.trakt.app.core.profile.usecases

import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalSource
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalSource
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.auth.usecase.RevokeTokenUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics

internal class LogoutProfileUseCase(
    private val apiClients: Array<ApiClient>,
    private val sessionManager: SessionManager,
    private val revokeTokenUseCase: RevokeTokenUseCase,
    private val showsSyncLocalSource: ShowsSyncLocalSource,
    private val moviesSyncLocalSource: MoviesSyncLocalSource,
    private val episodesSyncLocalSource: EpisodesSyncLocalSource,
    private val analytics: Analytics,
) {
    suspend fun logoutUser() {
        // Local sign-out must finish even when the screen that started it goes away.
        withContext(NonCancellable) {
            revokeTokenUseCase.revokeToken()
            sessionManager.clear()
            analytics.setUserId(null)
            analytics.setUserProperty("vip", null)

            apiClients
                .forEach { api ->
                    api.client.authProvider<BearerAuthProvider>()?.clearToken()
                }

            showsSyncLocalSource.clear()
            moviesSyncLocalSource.clear()
            episodesSyncLocalSource.clear()
        }
    }
}
