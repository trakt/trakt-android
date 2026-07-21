package tv.trakt.trakt.core.ratings.rateprompt.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.user.data.local.favorites.UserFavoritesLocalDataSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.core.favorites.FavoritesUpdates
import tv.trakt.trakt.core.favorites.FavoritesUpdates.Source.RATE_PROMPT
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState
import tv.trakt.trakt.core.sync.usecases.UpdateMovieFavoritesUseCase
import java.time.Instant

internal class RatePromptViewModel(
    private val media: RatePromptMedia,
    private val moreMedia: List<RatePromptMedia>,
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val ratePromptManager: RatePromptManager,
    private val updateMovieFavoritesUseCase: UpdateMovieFavoritesUseCase,
    private val userFavoritesLocalSource: UserFavoritesLocalDataSource,
    private val favoritesUpdates: FavoritesUpdates,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = RatePromptUiState()

    private val ratePromptState = MutableStateFlow(initialState.ratePrompt)
    private val ratingsState = MutableStateFlow(initialState.rating)
    private val favoriteState = MutableStateFlow(media.favorite)
    private val dismissingState = MutableStateFlow(initialState.dismissing)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var ratingJob: Job? = null

    init {
        observeRatePrompt()
    }

    @OptIn(FlowPreview::class)
    private fun observeRatePrompt() {
        ratePromptManager.observe()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { state ->
                ratePromptState.update { state }
            }
            .launchIn(viewModelScope)
    }

    fun addRating(newRating: Int) {
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            if (ratingsState.value == newRating) {
                Timber.d("Rating is already $newRating, skipping update")
                return@launch
            }

            ratingsState.update { newRating }
            dismissingState.update { nowUtcInstant() }

            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = media.movie.ids.trakt,
                mediaType = Movie,
                rating = newRating,
            )
        }
    }

    fun removeRating() {
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            if (ratingsState.value == null) {
                Timber.d("Rating is already gone, skipping removal")
                return@launch
            }

            ratingsState.update { null }
            dismissingState.update { nowUtcInstant() }

            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = media.movie.ids.trakt,
                mediaType = Movie,
                rating = 0, // A rating of 0 indicates removal of rating
            )
        }
    }

    fun addToFavorites() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                loadingState.update { Loading }
                dismissingState.update { nowUtcInstant() }

                delay(300) // Small delay to allow UI to settle.
                updateMovieFavoritesUseCase.addToFavorites(media.movie.ids.trakt)
                userFavoritesLocalSource.addMovies(
                    movies = listOf(
                        FavoriteItem.MovieItem(
                            rank = 0,
                            movie = media.movie,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                )
                favoritesUpdates.notifyUpdate(RATE_PROMPT)

                favoriteState.update { true }

                analytics.ratings.logFavoriteAdd(
                    mediaType = "movie",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun removeFromFavorites() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                loadingState.update { Loading }
                dismissingState.update { nowUtcInstant() }

                delay(300) // Small delay to allow UI to settle.
                updateMovieFavoritesUseCase.removeFromFavorites(media.movie.ids.trakt)
                userFavoritesLocalSource.removeMovies(setOf(media.movie.ids.trakt))
                favoritesUpdates.notifyUpdate(RATE_PROMPT)

                favoriteState.update { false }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun dismiss() {
        if (loadingState.value == Loading) {
            return
        }

        viewModelScope.launch {
            try {
                ratePromptManager.onUserDismiss(
                    movieId = media.movie.ids.trakt,
                    hasRated = ratingsState.value != null || favoriteState.value,
                    hasMoreMedia = moreMedia,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    val state = combine(
        ratePromptState,
        ratingsState,
        favoriteState,
        dismissingState,
        loadingState,
        errorState,
    ) { state ->
        RatePromptUiState(
            ratePrompt = state[0] as? RatePromptState,
            rating = state[1] as? Int,
            favorite = state[2] as Boolean,
            dismissing = state[3] as Instant?,
            loading = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
