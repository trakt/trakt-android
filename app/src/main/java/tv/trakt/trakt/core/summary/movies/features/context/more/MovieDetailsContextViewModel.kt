package tv.trakt.trakt.core.summary.movies.features.context.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieStreamingsUseCase

internal class MovieDetailsContextViewModel(
    private val movie: Movie,
    private val sessionManager: SessionManager,
    private val getStreamingsUseCase: GetMovieStreamingsUseCase,
) : ViewModel() {
    private val initialState = MovieDetailsContextState()

    private val streamingsState = MutableStateFlow(initialState.streamings)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = sessionManager.getProfile()
                .also { user ->
                    userState.update { user }
                }

            if (user == null) {
                streamingsState.update {
                    it.copy(
                        service = null,
                        noServices = true,
                    )
                }
                return@launch
            }

            try {
                streamingsState.update { it.copy(loading = true) }

                val streamingService = getStreamingsUseCase.getStreamingServices(
                    user = user,
                    movieId = movie.ids.trakt,
                )

                streamingsState.update {
                    it.copy(
                        slug = movie.ids.slug,
                        service = streamingService.streamingService,
                        noServices = streamingService.noServices,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                streamingsState.update { it.copy(loading = false) }
            }
        }
    }

    val state: StateFlow<MovieDetailsContextState> = combine(
        userState,
        streamingsState,
        errorState,
    ) { s1, s2, s3 ->
        MovieDetailsContextState(
            user = s1,
            streamings = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
