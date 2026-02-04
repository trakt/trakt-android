package tv.trakt.trakt.core.checkin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import tv.trakt.trakt.core.checkin.data.remote.CheckInRemoteDataSource
import tv.trakt.trakt.core.checkin.model.CheckInState
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private val ACTIVE_CHECK_COOLDOWN = 1.minutes

internal class DefaultCheckInManager(
    private val sessionManager: SessionManager,
    private val checkInRemoteDataSource: CheckInRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val cacheMarkerProvider: CacheMarkerProvider,
) : CheckInManager {
    private val state = MutableStateFlow<CheckInState>(CheckInState.Idle)

    private var lastCheckAt: Instant? = null

    override fun observe(): Flow<CheckInState> {
        return state.asStateFlow()
    }

    override suspend fun startMovie(movieId: TraktId) {
        if (!sessionManager.isAuthenticated()) {
            Timber.d("Not authenticated, skipping check-in.")
            return
        }

        state.update { CheckInState.Loading }
        Timber.d("Checking in movie with ID: ${movieId.value}")

        try {
            checkInRemoteDataSource.postMovieCheckIn(movieId)
            cacheMarkerProvider.invalidate()

            val watching = userRemoteDataSource.getWatchingNow()
            if (watching == null) {
                state.update { CheckInState.Idle }
                Timber.d("No active watching found after check-in.")
                return
            }

            watching.movie?.let { dto ->
                state.update {
                    CheckInState.ActiveMovie(
                        movie = Movie.fromDto(dto),
                        startedAt = watching.startedAt.toInstant(),
                        expiresAt = watching.expiresAt.toInstant(),
                    )
                }

                Timber.d("Successfully checked in movie: ${dto.title} (${dto.year})")
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                state.update { CheckInState.Error(error) }
                Timber.recordError(error)
            }
        }
    }

    override suspend fun checkActive() {
        if (!sessionManager.isAuthenticated()) {
            Timber.d("Not authenticated, skipping check-in stop.")
            return
        }

        lastCheckAt?.let { lastCheck ->
            val now = Clock.System.now()
            if (now.minus(ACTIVE_CHECK_COOLDOWN) < lastCheck) {
                Timber.d("Last check-in check was too recent, skipping redundant check.")
                return
            }
        }

        try {
            val response = userRemoteDataSource.getWatchingNow()
            if (response == null) {
                state.update { CheckInState.Idle }
                Timber.d("No active check-ins found.")
                return
            }

            response.movie?.let { dto ->
                state.update {
                    CheckInState.ActiveMovie(
                        movie = Movie.fromDto(dto),
                        startedAt = response.startedAt.toInstant(),
                        expiresAt = response.expiresAt.toInstant(),
                    )
                }
                Timber.d("Active movie check-in found: ${dto.title} (${dto.year})")
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        } finally {
            lastCheckAt = Clock.System.now()
        }
    }

    override suspend fun stop() {
        if (!sessionManager.isAuthenticated()) {
            Timber.d("Not authenticated, skipping check-in stop.")
            return
        }

        try {
            checkInRemoteDataSource.deleteAll()
            cacheMarkerProvider.invalidate()

            state.update { CheckInState.Idle }
            Timber.d("Stopped check-in.")
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }
}
