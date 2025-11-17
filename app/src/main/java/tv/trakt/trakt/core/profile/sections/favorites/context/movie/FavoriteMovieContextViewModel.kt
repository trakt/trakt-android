package tv.trakt.trakt.core.profile.sections.favorites.context.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.favorites.FavoritesUpdates
import tv.trakt.trakt.core.favorites.FavoritesUpdates.Source.CONTEXT_SHEET
import tv.trakt.trakt.core.sync.usecases.UpdateMovieFavoritesUseCase
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesLocalDataSource

internal class FavoriteMovieContextViewModel(
    private val movie: Movie,
    private val sessionManager: SessionManager,
    private val updateMovieFavoritesUseCase: UpdateMovieFavoritesUseCase,
    private val userFavoritesLocalSource: UserFavoritesLocalDataSource,
    private val favoritesUpdates: FavoritesUpdates,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = FavoriteMovieContextState()

    private val loadingState = MutableStateFlow(initialState.loading)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadUser()
        loadData()
    }

    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
        }
    }

    fun removeFromFavorites() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingState.update { LOADING }

                updateMovieFavoritesUseCase.removeFromFavorites(movie.ids.trakt)
                userFavoritesLocalSource.removeMovies(setOf(movie.ids.trakt))
                favoritesUpdates.notifyUpdate(CONTEXT_SHEET)

                analytics.ratings.logFavoriteRemove(
                    mediaType = MediaType.MOVIE.value,
                    source = "movie_context",
                )

                loadingState.update { DONE }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    val state: StateFlow<FavoriteMovieContextState> = combine(
        loadingState,
        userState,
        errorState,
    ) { state ->
        FavoriteMovieContextState(
            loading = state[0] as LoadingState,
            user = state[1] as User?,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
