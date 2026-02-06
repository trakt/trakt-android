package tv.trakt.trakt.core.checkin.model

import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.checkin.model.CheckInState.ActiveEpisode
import tv.trakt.trakt.core.checkin.model.CheckInState.ActiveMovie
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
        val show: Show,
        val episode: Episode,
        val startedAt: Instant,
        val expiresAt: Instant,
    ) : CheckInState

    fun isActive(): Boolean {
        return when (this) {
            is Idle, is Loading, is Error -> false
            is ActiveMovie, is ActiveEpisode -> true
        }
    }
}

val CheckInState.title: String?
    get() {
        return when (this) {
            is ActiveMovie -> movie.title
            is ActiveEpisode -> show.title
            else -> null
        }
    }

val CheckInState.image: String?
    get() {
        return when (this) {
            is ActiveMovie -> movie.images?.getFanartUrl(Size.THUMB)
            is ActiveEpisode -> episode.images?.getScreenshotUrl(Size.THUMB)
            else -> null
        }
    }

val CheckInState.startedAt: Instant?
    get() {
        return when (this) {
            is ActiveMovie -> this.startedAt
            is ActiveEpisode -> this.startedAt
            else -> null
        }
    }

val CheckInState.expiresAt: Instant?
    get() {
        return when (this) {
            is ActiveMovie -> this.expiresAt
            is ActiveEpisode -> this.expiresAt
            else -> null
        }
    }
