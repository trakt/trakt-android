package tv.trakt.trakt.core.summary.social.recommendedby.usecases

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3ShareMuteRequest
import tv.trakt.trakt.core.summary.social.recommendedby.model.RecommendedBy

internal class RecommendedByUseCase(
    private val remoteSource: V3Api,
    private val sessionManager: SessionManager,
) {
    suspend fun getRecommendedBy(path: String): RecommendedBy? {
        if (!sessionManager.isAuthenticated()) {
            return null
        }

        return remoteSource
            .getRecommendedBy(path)
            ?.let(RecommendedBy::fromDto)
    }

    suspend fun muteSharer(sharerId: TraktId) {
        remoteSource.postShareMute(
            V3ShareMuteRequest(sharerId = sharerId.value),
        )
    }
}
