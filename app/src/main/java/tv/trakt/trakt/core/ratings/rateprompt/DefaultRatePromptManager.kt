package tv.trakt.trakt.core.ratings.rateprompt

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia.MovieMedia
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia.ShowMedia
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState.AskSuppress
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState.Idle
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState.UnratedMedia
import tv.trakt.trakt.core.ratings.rateprompt.usecases.IsShowRatingCandidateUseCase
import tv.trakt.trakt.core.settings.usecases.UpdateUserSettingsUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinInstant

private const val USER_DISMISS_LIMIT = 5
private const val UNRATED_MEDIA_LIMIT = 5
private const val MOVIE_HISTORY_LIMIT = 10

/**
 * Episodes are pulled over the whole binge window so a single request answers both
 * "watched recently" and "how many episodes of this show this week".
 */
private const val EPISODE_HISTORY_LIMIT = 100

private val RECENTLY_WATCHED_DURATION = 24.hours.toJavaDuration()
private val SHOW_BINGE_DURATION = 7.days.toJavaDuration()

private val KEY_USER_DISMISS_COUNT = intPreferencesKey("key_dismiss_count")
private val KEY_DISMISSED_MEDIA = stringSetPreferencesKey("key_dismissed_media")

/**
 * Default implementation of [RatePromptManager] that checks the user's ratings and favorites to determine
 * if they should be prompted to rate recently watched movies and shows. It also tracks user dismissals and
 * suppresses the prompt after a certain number of dismissals.
 */
internal class DefaultRatePromptManager(
    private val sessionManager: SessionManager,
    private val checkInManager: CheckInManager,
    private val dataStore: DataStore<Preferences>,
    private val userRatingsUseCase: LoadUserRatingsUseCase,
    private val userFavoritesUseCase: LoadUserFavoritesUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    private val userHistoryDataSource: UserHistoryRemoteDataSource,
    private val isShowRatingCandidateUseCase: IsShowRatingCandidateUseCase,
) : RatePromptManager {
    private val state = MutableStateFlow<RatePromptState>(Idle)
    private var moviesFavorites = emptyList<FavoriteItem>()

    override suspend fun checkRecentlyWatched() {
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
            val recentlyWatchedWindow = nowUtc.minus(RECENTLY_WATCHED_DURATION)
            val bingeWindow = nowUtc.minus(SHOW_BINGE_DURATION)

            coroutineScope {
                val movieRatingsAsync = async {
                    if (!userRatingsUseCase.isMoviesLoaded()) {
                        userRatingsUseCase.loadMovies()
                    }
                }
                val showRatingsAsync = async {
                    if (!userRatingsUseCase.isShowsLoaded()) {
                        userRatingsUseCase.loadShows()
                    }
                }
                val favoritesAsync = async {
                    if (!userFavoritesUseCase.isMoviesLoaded()) {
                        userFavoritesUseCase.loadMovies()
                    }
                }
                awaitAll(movieRatingsAsync, showRatingsAsync, favoritesAsync)
            }

            val moviesRatings = userRatingsUseCase.loadLocalMovies()
            val showsRatings = userRatingsUseCase.loadLocalShows()
            moviesFavorites = userFavoritesUseCase.loadLocalMovies()

            val dismissed = loadDismissedMedia()

            val (movieHistory, episodeHistory) = coroutineScope {
                val moviesAsync = async {
                    userHistoryDataSource.getMoviesHistory(
                        page = 1,
                        limit = MOVIE_HISTORY_LIMIT,
                        filters = null,
                        from = recentlyWatchedWindow.toKotlinInstant(),
                    )
                }
                val episodesAsync = async {
                    userHistoryDataSource.getEpisodesHistory(
                        page = 1,
                        limit = EPISODE_HISTORY_LIMIT,
                        filters = null,
                        from = bingeWindow.toKotlinInstant(),
                    )
                }

                moviesAsync.await() to episodesAsync.await()
            }

            val movieItems = movieHistory.asyncMap {
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
            }
            val episodeItems = episodeHistory.asyncMap {
                HomeActivityItem.EpisodeItem(
                    id = it.id,
                    user = null,
                    userRating = null,
                    activity = it.action.value,
                    activityAt = it.watchedAt.toInstant(),
                    episode = Episode.fromDto(
                        checkNotNull(it.episode) {
                            "Episode should not be null if type is EPISODE"
                        },
                    ),
                    show = Show.fromDto(
                        checkNotNull(it.show) {
                            "Show should not be null if type is EPISODE"
                        },
                    ),
                )
            }

            // Every episode in the payload already sits inside the binge window.
            val showPlaysInBingeWindow = episodeItems.groupingBy { it.show.ids.trakt }.eachCount()

            // Only consider media the user hasn't rated, favorited, or dismissed, that was watched
            // within the recently watched duration, and - for shows - that hit a rating moment.
            val media = (movieItems + episodeItems)
                .filter { it.activityAt >= recentlyWatchedWindow }
                .sortedByDescending { it.activityAt }
                .mapNotNull {
                    it.toPromptMedia(
                        moviesRatings = moviesRatings,
                        showsRatings = showsRatings,
                        dismissed = dismissed,
                        showPlaysInBingeWindow = showPlaysInBingeWindow,
                    )
                }
                .distinctBy { it.mediaType to it.id }
                .take(UNRATED_MEDIA_LIMIT)

            if (media.isEmpty()) {
                Timber.d("No recently watched media found, skipping rate prompt.")
                return
            } else {
                Timber.d("${media.size} items for rate prompt: ${media.joinToString { it.title }}")
            }

            state.update { UnratedMedia(media.toImmutableList()) }
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
        media: RatePromptMedia,
        hasRated: Boolean,
        hasMoreMedia: List<RatePromptMedia>,
    ) {
        val now = nowUtcInstant()

        dataStore.edit {
            val dismissedMedia = it.dismissedMedia(now).toMutableSet()
            dismissedMedia.add(media.toDismissalEntry(now))
            it[KEY_DISMISSED_MEDIA] = dismissedMedia

            if (!hasRated) {
                // Only increment the dismiss count if the user dismissed the prompt without rating the media.
                val dismissCount = it[KEY_USER_DISMISS_COUNT] ?: 0
                it[KEY_USER_DISMISS_COUNT] = dismissCount + 1
                Timber.d(
                    "User dismissed rate prompt for %s. Dismiss count: %s",
                    media.dismissalKey(),
                    dismissCount + 1,
                )
            } else {
                // If the user rated the media, reset the dismiss count.
                it[KEY_USER_DISMISS_COUNT] = 0
                Timber.d("User rated %s after dismissing. Resetting dismiss count.", media.dismissalKey())
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
                state.update { UnratedMedia(hasMoreMedia.toImmutableList()) }
            }
        }
    }

    override suspend fun onUserSuppress() {
        clear()
        try {
            updateUserSettingsUseCase.updateRatingsPrompt(false)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    override fun clear() {
        state.update { Idle }
    }

    private suspend fun loadDismissedMedia(): Set<String> {
        val now = nowUtcInstant()

        return dataStore.data
            .map { it.dismissedMedia(now) }
            .firstOrNull()
            .orEmpty()
            .mapTo(mutableSetOf()) { it.substringBeforeLast(DISMISSAL_SEPARATOR) }
    }

    /** Dismissals only hold for the recently watched window, mirroring how long the prompt can surface an item. */
    private fun Preferences.dismissedMedia(now: Instant): Set<String> {
        val cutoff = now.minus(RECENTLY_WATCHED_DURATION).toEpochMilli()

        return this[KEY_DISMISSED_MEDIA]
            .orEmpty()
            .filter { entry ->
                val dismissedAt = entry.substringAfterLast(DISMISSAL_SEPARATOR).toLongOrNull()
                dismissedAt != null && dismissedAt >= cutoff
            }
            .toSet()
    }

    private fun RatePromptMedia.toDismissalEntry(now: Instant): String {
        return "${mediaType.value}$DISMISSAL_SEPARATOR${id.value}$DISMISSAL_SEPARATOR${now.toEpochMilli()}"
    }

    private fun HomeActivityItem.toPromptMedia(
        moviesRatings: ImmutableMap<TraktId, UserRating>,
        showsRatings: ImmutableMap<TraktId, UserRating>,
        dismissed: Set<String>,
        showPlaysInBingeWindow: Map<TraktId, Int>,
    ): RatePromptMedia? {
        return when (this) {
            is HomeActivityItem.MovieItem -> {
                val media = MovieMedia(
                    movie = movie,
                    favorite = moviesFavorites.any { it.id == movie.ids.trakt },
                )
                val isEligible = !moviesRatings.contains(movie.ids.trakt) &&
                    !dismissed.contains(media.dismissalKey()) &&
                    checkInManager.isActiveMovie()?.movie?.ids?.trakt != movie.ids.trakt

                media.takeIf { isEligible }
            }

            is HomeActivityItem.EpisodeItem -> {
                val media = ShowMedia(show = show)
                val isEligible = !showsRatings.contains(show.ids.trakt) &&
                    !dismissed.contains(media.dismissalKey()) &&
                    isShowRatingCandidateUseCase(
                        episodeType = episode.type,
                        showPlaysInBingeWindow = showPlaysInBingeWindow[show.ids.trakt] ?: 0,
                    )

                media.takeIf { isEligible }
            }
        }
    }

    private fun RatePromptMedia.dismissalKey(): String {
        return "${mediaType.value}$DISMISSAL_SEPARATOR${id.value}"
    }

    private companion object {
        const val DISMISSAL_SEPARATOR = ":"
    }
}
