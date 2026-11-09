package tv.trakt.trakt.common.core.user.usecases.spoilers

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.model.TraktId

class GetSpoilerStateUseCase(
    private val sessionManager: SessionManager,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val showLocalSource: ShowLocalSource,
) {
    suspend fun isEnabled(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            return false
        }
        return sessionManager.getProfile()?.settings?.hideSpoilers == true
    }

    suspend fun isShowHidden(showId: TraktId): Boolean {
        if (!isEnabled()) {
            return false
        }

        val airedEpisodes = showLocalSource.getShow(showId)?.airedEpisodes ?: 0
        if (airedEpisodes <= 0) {
            return true
        }

        val shows = when {
            loadUserProgressUseCase.isShowsLoaded() -> loadUserProgressUseCase.loadLocalShows()
            else -> loadUserProgressUseCase.loadShowsProgress()
        }
        val watchedEpisodes = shows
            .firstOrNull { it.showId == showId }
            ?.playsDistinctWithoutSpecials
            ?: 0

        return watchedEpisodes < airedEpisodes
    }
}
