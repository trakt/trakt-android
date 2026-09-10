package tv.trakt.trakt.core.summary.movies.features.reactions.usecases

import kotlinx.coroutines.delay
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.core.reactions.media.MediaReactionEmoji
import kotlin.time.Duration.Companion.milliseconds

internal class UpdateMovieReactionsUseCase(
    @Suppress("unused")
    private val remoteSource: V3Api,
    private val sessionManager: SessionManager,
) {
    suspend fun updateReactions(
        movieId: TraktId,
        reactions: List<MediaReactionEmoji>,
    ) {
        if (!sessionManager.isAuthenticated()) return

        // TODO: diff against the user's current reactions and call
        //  remoteSource.putReactions / deleteReactions (MediaType.Movie) once
        //  reaction type names and ids are confirmed. Stubbed with fake latency.
        delay(400.milliseconds)
    }
}
