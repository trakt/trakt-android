package tv.trakt.trakt.core.ratings.rateprompt

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState.AskSuppress
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState.Idle
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState.UnratedMovies
import tv.trakt.trakt.core.settings.usecases.UpdateUserSettingsUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

private const val USER_DISMISS_LIMIT = 5
private const val UNRATED_MOVIES_LIMIT = 5
private val RECENTLY_WATCHED_DURATION = 24.hours.toJavaDuration()

private val KEY_USER_DISMISS_COUNT = intPreferencesKey("key_dismiss_count")
private val KEY_DISMISSED_MOVIES = stringSetPreferencesKey("key_dismissed_movies")

/**
 * Default implementation of [RatePromptManager] that checks the user's ratings and favorites to determine
 * if they should be prompted to rate recently watched movies. It also tracks user dismissals and suppresses
 * the prompt after a certain number of dismissals.
 */
internal class DefaultRatePromptManager(
    private val sessionManager: SessionManager,
    private val checkInManager: CheckInManager,
    private val dataStore: DataStore<Preferences>,
    private val userRatingsUseCase: LoadUserRatingsUseCase,
    private val userFavoritesUseCase: LoadUserFavoritesUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    private val userHistoryDataSource: UserHistoryRemoteDataSource,
) : RatePromptManager {
    private val state = MutableStateFlow<RatePromptState>(Idle)
    private var moviesFavorites = emptyList<FavoriteItem>()

    override suspend fun checkMovies() {
        if (!sessionManager.isAuthenticated()) {
            Timber.d("User is not authenticated, skipping rate prompt.")
            return
        }

        if (sessionManager.getProfile()?.settings?.ratingPrompts != true) {
            Timber.d("Rate prompt is disabled.")
            return
        }

        try {
            val nowUtc = nowUtcInstant()
            val durationWindow = nowUtc.minus(RECENTLY_WATCHED_DURATION)

            coroutineScope {
                val ratingsCase = async {
                    if (!userRatingsUseCase.isMoviesLoaded()) {
                        userRatingsUseCase.loadMovies()
                    }
                }
                val favoritesAsync = async {
                    if (!userFavoritesUseCase.isMoviesLoaded()) {
                        userFavoritesUseCase.loadMovies()
                    }
                }
                awaitAll(ratingsCase, favoritesAsync)
            }

            val moviesRatings = userRatingsUseCase.loadLocalMovies()
            moviesFavorites = userFavoritesUseCase.loadLocalMovies()

            val moviesDismissed = dataStore.data
                .map { it[KEY_DISMISSED_MOVIES] ?: emptySet() }
                .firstOrNull() ?: emptySet()

            // Only consider movies that the user hasn't rated, favorite, or dismissed,
            // and were watched within the recently watched duration.
            val movies = userHistoryDataSource.getMoviesHistory(
                page = 1,
                limit = 10,
                filters = null,
            ).asyncMap {
                HomeActivityItem.MovieItem(
                    id = it.id,
                    user = null,
                    userRating = null,
                    activity = it.action.value,
                    activityAt = it.watchedAt.toInstant(),
                    movie = Movie.fromDto(
                        checkNotNull(it.movie) {
                            "Movie should not be null if type is MOVIE"
                        },
                    ),
                )
            }.filter {
                !moviesRatings.contains(it.movie.ids.trakt) &&
                    !moviesDismissed.contains(it.movie.ids.trakt.value.toString()) &&
                    checkInManager.isActiveMovie()?.movie?.ids?.trakt != it.movie.ids.trakt &&
                    it.activityAt >= durationWindow
            }.take(UNRATED_MOVIES_LIMIT)

            if (movies.isEmpty()) {
                Timber.d("No recently watched movies found, skipping rate prompt.")
                return
            } else {
                Timber.d("${movies.size} movies for rate prompt: ${movies.joinToString { it.movie.title }}")
            }

            state.update {
                UnratedMovies(
                    movies = movies
                        .sortedByDescending { it.activityAt }
                        .map { it.movie }
                        .toImmutableList(),
                    favorites = moviesFavorites
                        .map { it.id }
                        .toImmutableSet(),
                )
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    override fun observe(): Flow<RatePromptState> {
        return state.asStateFlow()
    }

    override suspend fun onUserDismiss(
        movieId: TraktId,
        hasRated: Boolean,
        hasMoreMedia: List<RatePromptMedia>,
    ) {
        dataStore.edit {
            val dismissedMovies = it[KEY_DISMISSED_MOVIES]?.toMutableSet() ?: mutableSetOf()
            dismissedMovies.add(movieId.value.toString())
            it[KEY_DISMISSED_MOVIES] = dismissedMovies

            if (!hasRated) {
                // Only increment the dismiss count if the user dismissed the prompt without rating the movie.
                val dismissCount = it[KEY_USER_DISMISS_COUNT] ?: 0
                it[KEY_USER_DISMISS_COUNT] = dismissCount + 1
                Timber.d("User dismissed rate prompt for movie ${movieId.value}. Dismiss count: ${dismissCount + 1}")
            } else {
                // If the user rated the movie, reset the dismiss count.
                it[KEY_USER_DISMISS_COUNT] = 0
                Timber.d("User rated movie ${movieId.value} after dismissing. Resetting dismiss count.")
            }
        }

        // After updating the dismiss count, check if the user has reached the dismissal limit.
        val dismissCount = dataStore.data.firstOrNull()?.get(KEY_USER_DISMISS_COUNT) ?: 0
        if (dismissCount > 0 && dismissCount % USER_DISMISS_LIMIT == 0) {
            // If the user has dismissed the prompt a multiple of the dismissal limit, ask to suppress future prompts.
            state.update { AskSuppress }
        } else {
            // If there are more media to show, update the state with the remaining media.
            if (hasMoreMedia.isEmpty()) {
                state.update { Idle }
            } else {
                state.update {
                    UnratedMovies(
                        movies = hasMoreMedia
                            .map { it.movie }
                            .toImmutableList(),
                        favorites = moviesFavorites
                            .map { it.id }
                            .toImmutableSet(),
                    )
                }
            }
        }
    }

    override suspend fun onUserSuppress() {
        updateUserSettingsUseCase.updateRatingsPrompt(false)
        clear()
    }

    override fun clear() {
        state.update { Idle }
    }
}
