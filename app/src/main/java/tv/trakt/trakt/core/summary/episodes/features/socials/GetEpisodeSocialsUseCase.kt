package tv.trakt.trakt.core.summary.episodes.features.socials

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.MediaType.EPISODE
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity

internal class GetEpisodeSocialsUseCase(
    val remoteSource: V3Api,
    private val sessionManager: SessionManager,
) {
    suspend fun getSocials(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ImmutableList<MediaSocialActivity> {
        if (!sessionManager.isAuthenticated()) {
            return EmptyImmutableList
        }

        val remoteData = remoteSource.getEpisodeSocialActivity(
            showId = showId,
            season = season,
            episode = episode,
            pagination = Pagination(1, 250),
        )
        if (remoteData.isNullOrEmpty()) {
            return EmptyImmutableList
        }

        return remoteData
            .map { MediaSocialActivity.fromDto(it, EPISODE) }
            .sortedByDescending { it.lastActivityAt }
            .toImmutableList()
    }
}
