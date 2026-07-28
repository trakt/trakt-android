package tv.trakt.trakt.core.checkin.data

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import tv.trakt.trakt.core.checkin.data.remote.CheckInRemoteDataSource
import tv.trakt.trakt.core.checkin.data.service.CheckInService
import tv.trakt.trakt.core.checkin.data.service.CheckInServiceData
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates.Source
import tv.trakt.trakt.core.checkin.model.CheckInState
import tv.trakt.trakt.core.checkin.model.CheckInState.ActiveEpisode
import tv.trakt.trakt.core.checkin.model.CheckInState.ActiveMovie
import tv.trakt.trakt.core.checkin.model.expiresAt
import tv.trakt.trakt.core.checkin.model.id
import tv.trakt.trakt.core.checkin.model.startedAt
import tv.trakt.trakt.core.checkin.model.type
import tv.trakt.trakt.resources.R
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val ACTIVE_CHECK_COOLDOWN = 10.seconds

internal class DefaultCheckInManager(
    private val sessionManager: SessionManager,
    private val checkInUpdates: CheckInUpdates,
    private val checkInRemoteDataSource: CheckInRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val cacheMarkerProvider: CacheMarkerProvider,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val loadUserWatchlistMinUseCase: LoadUserWatchlistUseCase,
) : CheckInManager {
    private val state = MutableStateFlow<CheckInState>(CheckInState.Idle)
    private var lastCheckAt: Instant? = null

    override fun observe(): Flow<CheckInState> {
        return state.asStateFlow()
    }

    override suspend fun startEpisode(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        source: Source,
        context: Context,
    ) {
        if (state.value.isActive()) {
            Timber.d("Another check-in is already active, skipping new episode check-in.")
            return
        }

        if (!sessionManager.isAuthenticated()) {
            Timber.d("Not authenticated, skipping check-in.")
            return
        }

        state.update { CheckInState.Loading }
        Timber.d("Checking in episode $seasonEpisode of show: ${showId.value}")

        try {
            checkInRemoteDataSource.postEpisodeCheckIn(
                showId = showId,
                season = seasonEpisode.season,
                episode = seasonEpisode.episode,
            )
            cacheMarkerProvider.invalidate()

            delay(300.milliseconds)
            var watching = userRemoteDataSource.getWatchingNow()
            if (watching == null) {
                // Wait a bit longer for the check-in to be reflected in replica.
                delay(2.seconds)
                watching = userRemoteDataSource.getWatchingNow()
                if (watching == null) {
                    throw Exception("Oops! No active watching now found after check-in.")
                }
            }

            watching.episode?.let { dto ->
                val activeState = ActiveEpisode(
                    show = Show.fromDto(watching.show!!),
                    episode = Episode.fromDto(dto),
                    startedAt = watching.startedAt.toInstant(),
                    expiresAt = watching.expiresAt.toInstant(),
                )

                state.update { activeState }
                checkInUpdates.notifyUpdate(source)
                startForegroundService(context, activeState)

                coroutineScope {
                    val progressAsync = async { loadProgressIfNeeded(activeState) }
                    val watchlistAsync = async { loadWatchlistIfNeeded(activeState) }
                    awaitAll(progressAsync, watchlistAsync)
                }

                Timber.d("Successfully checked in episode: ${dto.title} S${dto.season}E${dto.number}")
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                state.update { CheckInState.Error(error) }
                Timber.recordError(error)
            }
        }
    }

    override suspend fun startMovie(
        movieId: TraktId,
        source: Source,
        context: Context,
    ) {
        if (state.value.isActive()) {
            Timber.d("Another check-in is already active, skipping new movie check-in.")
            return
        }

        if (!sessionManager.isAuthenticated()) {
            Timber.d("Not authenticated, skipping check-in.")
            return
        }

        state.update { CheckInState.Loading }
        Timber.d("Checking in movie with ID: ${movieId.value}")

        try {
            checkInRemoteDataSource.postMovieCheckIn(movieId)
            cacheMarkerProvider.invalidate()

            delay(300.milliseconds)
            var watching = userRemoteDataSource.getWatchingNow()
            if (watching == null) {
                // Wait a bit longer for the check-in to be reflected in replica.
                delay(2.seconds)
                watching = userRemoteDataSource.getWatchingNow()
                if (watching == null) {
                    throw Exception("Oops! No active watching now found after check-in.")
                }
            }

            watching.movie?.let { dto ->
                val activeState = ActiveMovie(
                    movie = Movie.fromDto(dto),
                    startedAt = watching.startedAt.toInstant(),
                    expiresAt = watching.expiresAt.toInstant(),
                )

                state.update { activeState }
                checkInUpdates.notifyUpdate(source)
                startForegroundService(context, activeState)

                coroutineScope {
                    val progressAsync = async { loadProgressIfNeeded(activeState) }
                    val watchlistAsync = async { loadWatchlistIfNeeded(activeState) }
                    awaitAll(progressAsync, watchlistAsync)
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

    override fun isActive(): Boolean {
        return when (state.value) {
            is ActiveMovie, is ActiveEpisode -> true
            else -> false
        }
    }

    override fun isActiveMovie(): ActiveMovie? {
        return state.value as? ActiveMovie
    }

    override suspend fun checkActive(context: Context) {
        if (!sessionManager.isAuthenticated()) {
            Timber.d("Not authenticated, skipping check-in stop.")
            return
        }

        lastCheckAt?.let { lastCheck ->
            if (now().minus(ACTIVE_CHECK_COOLDOWN) < lastCheck) {
                Timber.d("Last check-in check was too recent, skipping redundant check.")
                return
            }
        }

        try {
            val response = userRemoteDataSource.getWatchingNow()
            if (response == null) {
                cacheMarkerProvider.invalidate()

                if (state.value.isActive()) {
                    loadProgressIfNeeded(state.value)
                    checkInUpdates.notifyUpdate(Source.Default)
                }

                stop(Source.Default, context)
                Timber.d("No active check-ins / scrobbles found.")
                return
            }

            response.movie?.let { dto ->
                val newState = ActiveMovie(
                    movie = Movie.fromDto(dto),
                    startedAt = response.startedAt.toInstant(),
                    expiresAt = response.expiresAt.toInstant(),
                )

                // Only update state if it's a different movie than currently stored
                if (state.value !is ActiveMovie || state.value.id != newState.id) {
                    cacheMarkerProvider.invalidate()
                    state.update { newState }
                    checkInUpdates.notifyUpdate(Source.Default)
                    startForegroundService(context, newState)

                    coroutineScope {
                        val progressAsync = async { loadProgressIfNeeded(newState) }
                        val watchlistAsync = async { loadWatchlistIfNeeded(newState) }
                        awaitAll(progressAsync, watchlistAsync)
                    }

                    Timber.d("New active movie check-in found: ${dto.title} (${dto.year})")
                }
            }

            response.episode?.let { dto ->
                val newState = ActiveEpisode(
                    show = Show.fromDto(response.show!!),
                    episode = Episode.fromDto(dto),
                    startedAt = response.startedAt.toInstant(),
                    expiresAt = response.expiresAt.toInstant(),
                )

                // Only update state if it's a different episode than currently stored
                if (state.value !is ActiveEpisode || state.value.id != newState.id) {
                    cacheMarkerProvider.invalidate()
                    state.update { newState }
                    checkInUpdates.notifyUpdate(Source.Default)
                    startForegroundService(context, newState)

                    coroutineScope {
                        val progressAsync = async { loadProgressIfNeeded(newState) }
                        val watchlistAsync = async { loadWatchlistIfNeeded(newState) }
                        awaitAll(progressAsync, watchlistAsync)
                    }

                    Timber.d("New active episode check-in found: ${dto.title} S${dto.season}E${dto.number}")
                }
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        } finally {
            lastCheckAt = now()
        }
    }

    override suspend fun stop(
        source: Source,
        context: Context,
    ) {
        if (!sessionManager.isAuthenticated()) {
            Timber.d("Not authenticated, skipping check-in stop.")
            return
        }

        val currentState = state.value
        state.update { CheckInState.Idle }

        try {
            checkInRemoteDataSource.deleteAll()
            cacheMarkerProvider.invalidate()

            if (currentState != CheckInState.Idle) {
                checkInUpdates.notifyUpdate(source)
            }
            CheckInService.stop(context.applicationContext)

            loadProgressIfNeeded(currentState)

            Timber.d("Stopped check-in.")
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    private suspend fun loadProgressIfNeeded(state: CheckInState) {
        try {
            when (state) {
                is ActiveMovie -> loadUserProgressUseCase.loadMoviesProgress()
                is ActiveEpisode -> loadUserProgressUseCase.loadShowsProgress()
                else -> Unit
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                // Log but ignore errors here
                Timber.recordError(error)
            }
        }
    }

    private suspend fun loadWatchlistIfNeeded(state: CheckInState) {
        try {
            when (state) {
                is ActiveMovie, is ActiveEpisode -> {
                    loadUserWatchlistMinUseCase.loadWatchlist()
                }
                else -> {}
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                // Log but ignore errors here
                Timber.recordError(error)
            }
        }
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

        val episodeState = state as? ActiveEpisode
        val data = CheckInServiceData(
            mediaId = state.id ?: -1,
            mediaType = type,
            title = when (state) {
                is ActiveMovie -> {
                    state.movie.title
                }
                is ActiveEpisode -> {
                    val seasonEpisode =
                        context.getString(
                            R.string.episode_footer_season_episode,
                            state.episode.season,
                            state.episode.number,
                        )
                    "${state.show.title} - $seasonEpisode"
                }
                else -> {
                    ""
                }
            },
            mediaTitle = when (state) {
                is ActiveMovie -> state.movie.title
                is ActiveEpisode -> state.show.title
                else -> null
            },
            mediaImage = when (state) {
                is ActiveMovie -> state.movie.images?.getFanartUrl()
                is ActiveEpisode -> state.show.images?.getFanartUrl()
                else -> null
            },
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
