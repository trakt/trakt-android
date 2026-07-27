package tv.trakt.trakt.core.summary.shows.features.seasons.all.helpers

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.ratings.data.RatingsUpdates
import tv.trakt.trakt.core.ratings.data.RatingsUpdates.Source.POST_RATING
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker
import tv.trakt.trakt.core.summary.shows.features.seasons.all.AllShowSeasonsState
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import kotlin.time.Duration.Companion.milliseconds

/**
 * Owns the selected season's user-rating state for the all-seasons screen. Scoped to the hosting
 * ViewModel via [scope]; the ViewModel folds [rating] into its composed UI state and delegates the
 * action calls here. Refresh/add/remove operate on whichever season is currently selected, read
 * through [currentSeason] so the controller stays decoupled from the ViewModel's items state.
 */
@OptIn(FlowPreview::class)
internal class SeasonRatingController(
    private val scope: CoroutineScope,
    private val appContext: Context,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
    private val sessionManager: SessionManager,
    ratingsUpdates: RatingsUpdates,
    private val currentSeason: () -> Season?,
) {
    private val seasonUserRatingState =
        MutableStateFlow(AllShowSeasonsState.UserRatingState())

    val rating: StateFlow<AllShowSeasonsState.UserRatingState> =
        seasonUserRatingState.asStateFlow()

    private var ratingJob: Job? = null

    init {
        ratingsUpdates.observeUpdates(POST_RATING)
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                refreshSeasonUserRating()
            }
            .launchIn(scope)
    }

    fun loadSeasonUserRating(season: Season) {
        scope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                seasonUserRatingState.update { it.copy(loading = Loading) }

                if (!loadUserRatingsUseCase.isSeasonsLoaded()) {
                    loadUserRatingsUseCase.loadSeasons()
                }

                seasonUserRatingState.update {
                    AllShowSeasonsState.UserRatingState(
                        rating = loadUserRatingsUseCase.loadLocalSeasons()[season.ids.trakt],
                        loading = Done,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                    seasonUserRatingState.update { it.copy(loading = Done) }
                }
            }
        }
    }

    private fun refreshSeasonUserRating() {
        val season = currentSeason() ?: return
        scope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                val rating = loadUserRatingsUseCase.loadLocalSeasons()[season.ids.trakt]
                seasonUserRatingState.update {
                    it.copy(rating = rating, loading = Done)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun addSeasonRating(newRating: Int) {
        val season = currentSeason() ?: return
        ratingJob?.cancel()
        ratingJob = scope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            if (seasonUserRatingState.value.rating?.rating == newRating) {
                return@launch
            }

            seasonUserRatingState.update {
                AllShowSeasonsState.UserRatingState(
                    rating = UserRating(
                        mediaId = season.ids.trakt,
                        mediaType = MediaType.Season,
                        rating = newRating,
                    ),
                    loading = Done,
                )
            }

            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = season.ids.trakt,
                mediaType = MediaType.Season,
                rating = newRating,
            )
        }
    }

    fun removeSeasonRating() {
        val season = currentSeason() ?: return
        ratingJob?.cancel()
        ratingJob = scope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            if (seasonUserRatingState.value.rating?.rating == 0) {
                return@launch
            }

            seasonUserRatingState.update {
                AllShowSeasonsState.UserRatingState(
                    rating = UserRating(
                        mediaId = season.ids.trakt,
                        mediaType = MediaType.Season,
                        rating = 0,
                    ),
                    loading = Done,
                )
            }

            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = season.ids.trakt,
                mediaType = MediaType.Season,
                rating = 0, // A rating of 0 indicates removal of rating
            )
        }
    }
}
