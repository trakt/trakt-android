package tv.trakt.trakt.core.applinks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.applinks.model.AppLink
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import java.net.HttpURLConnection.HTTP_NOT_FOUND

@Suppress("UNCHECKED_CAST")
internal class AppLinkViewModel(
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
) : ViewModel() {
    private val initialState = AppLinkState()

    private val loadingState = MutableStateFlow(initialState.loading)

    private var openJob: Job? = null

    fun openAppLink(link: AppLink) {
        openJob?.cancel()
        openJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                // Links carry slugs, while details destinations expect Trakt IDs.
                val event = when (link) {
                    is AppLink.Show -> AppLinkEvent.OpenShow(
                        showId = getShowDetailsUseCase.getShow(link.slug).ids.trakt,
                    )
                    is AppLink.Movie -> AppLinkEvent.OpenMovie(
                        movieId = getMovieDetailsUseCase.getMovie(link.slug).ids.trakt,
                    )
                }
                loadingState.update { Done }
                events.emit(event)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (error.getHttpCode() != HTTP_NOT_FOUND) {
                        Timber.recordError(error)
                    }
                }
                loadingState.update { Done }
                events.emit(AppLinkEvent.Error)
            }
        }
    }

    fun cancelAppLink() {
        openJob?.cancel()
        loadingState.update { Idle }
    }

    val events: Flow<AppLinkEvent>
        field = MutableSharedFlow<AppLinkEvent>(replay = 0)

    val state = combine(
        loadingState,
    ) { state ->
        AppLinkState(
            loading = state[0] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
