package tv.trakt.trakt.core.summary.movies.features.socials

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity

internal class GetMovieSocialsUseCase(
    private val remoteSource: V3Api,
    private val sessionManager: SessionManager,
) {
    suspend fun getSocials(movieId: TraktId): ImmutableList<MediaSocialActivity> {
        if (!sessionManager.isAuthenticated()) {
            return EmptyImmutableList
        }

        val remoteData = remoteSource.getMovieSocialActivity(movieId, Pagination(1, 250))
        if (remoteData.isNullOrEmpty()) {
            return EmptyImmutableList
        }

        return remoteData
            .map { MediaSocialActivity.fromDto(it, MOVIE) }
            .sortedByDescending { it.lastActivityAt }
            .toImmutableList()
    }
}
