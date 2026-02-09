package tv.trakt.trakt.core.checkin.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import tv.trakt.trakt.core.checkin.data.remote.CheckInRemoteDataSource
import tv.trakt.trakt.core.checkin.data.service.CheckInService
import tv.trakt.trakt.core.checkin.data.service.CheckInServiceData
import tv.trakt.trakt.core.checkin.model.CheckInState
import tv.trakt.trakt.core.checkin.model.expiresAt
import tv.trakt.trakt.core.checkin.model.id
import tv.trakt.trakt.core.checkin.model.startedAt
import tv.trakt.trakt.core.checkin.model.title
import tv.trakt.trakt.core.checkin.model.type
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val ACTIVE_CHECK_COOLDOWN = 15.seconds

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

    override suspend fun checkActive(context: Context) {
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
                clear(context)
                Timber.d("No active check-ins / scrobbles found.")
                return
            }

            response.movie?.let { dto ->
                val newState = CheckInState.ActiveMovie(
                    movie = Movie.fromDto(dto),
                    startedAt = response.startedAt.toInstant(),
                    expiresAt = response.expiresAt.toInstant(),
                )

                // Only update state if it's a different movie than currently stored
                if (state.value !is CheckInState.ActiveMovie || state.value.id != newState.id) {
                    state.update { newState }
                    startForegroundService(context, newState)
                    Timber.d("New active movie check-in found: ${dto.title} (${dto.year})")
                }
            }

            response.episode?.let { dto ->
                val newState = CheckInState.ActiveEpisode(
                    show = Show.fromDto(response.show!!),
                    episode = Episode.fromDto(dto),
                    startedAt = response.startedAt.toInstant(),
                    expiresAt = response.expiresAt.toInstant(),
                )

                // Only update state if it's a different episode than currently stored
                if (state.value !is CheckInState.ActiveEpisode || state.value.id != newState.id) {
                    state.update { newState }
                    startForegroundService(context, newState)
                    Timber.d("New active episode check-in found: ${dto.title} S${dto.season}E${dto.number}")
                }
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        } finally {
            lastCheckAt = Clock.System.now()
        }
    }

    override suspend fun stop(context: Context) {
        if (!sessionManager.isAuthenticated()) {
            Timber.d("Not authenticated, skipping check-in stop.")
            return
        }

        try {
            checkInRemoteDataSource.deleteAll()
            cacheMarkerProvider.invalidate()

            state.update { CheckInState.Idle }
            CheckInService.stop(context.applicationContext)

            Timber.d("Stopped check-in.")
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    override fun clear(context: Context) {
        state.update { CheckInState.Idle }
        CheckInService.stop(context.applicationContext)

        Timber.d("Cleared check-in state.")
    }

    private fun startForegroundService(
        context: Context,
        state: CheckInState,
    ) {
        val type = state.type
        val startedAt = state.startedAt
        val expiresAt = state.expiresAt

        if (type == null || startedAt == null || expiresAt == null) {
            Timber.d("Invalid check-in state for starting service, missing required data.")
            return
        }

        val episodeState = state as? CheckInState.ActiveEpisode
        val data = CheckInServiceData(
            mediaId = state.id ?: -1,
            mediaType = type,
            title = state.title ?: "",
            startedAt = startedAt,
            expiresAt = expiresAt,
            extraId = episodeState?.show?.ids?.trakt?.value,
            extraValue1 = episodeState?.episode?.season,
            extraValue2 = episodeState?.episode?.number,
        )

        CheckInService.start(
            context = context,
            data = data,
        )
    }
}
