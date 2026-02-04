package tv.trakt.trakt.core.checkin.model

import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

sealed interface CheckInState {
    object Idle : CheckInState

    object Loading : CheckInState

    data class Error(
        val error: Exception,
    ) : CheckInState

    data class ActiveMovie(
        val movie: Movie,
        val startedAt: Instant,
        val expiresAt: Instant,
    ) : CheckInState

    data class ActiveEpisode(
        val showId: TraktId,
        val episodeId: TraktId,
        val episode: SeasonEpisode,
        val startedAt: Instant,
        val expiresAt: Instant,
    ) : CheckInState
}
