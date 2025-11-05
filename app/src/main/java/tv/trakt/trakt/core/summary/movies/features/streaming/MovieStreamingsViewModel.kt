package tv.trakt.trakt.core.summary.movies.features.streaming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.core.summary.movies.features.streaming.usecases.GetMovieStreamingsUseCase

internal class MovieStreamingsViewModel(
    private val movie: Movie,
    private val sessionManager: SessionManager,
    private val getStreamingsUseCase: GetMovieStreamingsUseCase,
) : ViewModel() {
    private val initialState = MovieStreamingsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                val user = sessionManager.getProfile()
                if (user == null) {
                    itemsState.update { null }
                    loadingState.update { DONE }
                    return@launch
                }

                val items = getStreamingsUseCase.getStreamings(
                    user = user,
                    movieId = movie.ids.trakt,
                )

                itemsState.update { items }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<MovieStreamingsState> = combine(
        itemsState,
        loadingState,
        errorState,
    ) { state ->
        MovieStreamingsState(
            items = state[0] as ImmutableList<Pair<StreamingService, StreamingType>>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
