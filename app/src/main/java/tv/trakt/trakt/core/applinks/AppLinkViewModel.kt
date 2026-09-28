package tv.trakt.trakt.core.applinks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.applinks.model.AppLink
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import java.net.HttpURLConnection.HTTP_NOT_FOUND

internal class AppLinkViewModel(
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
) : ViewModel() {
    private var openJob: Job? = null

    fun openAppLink(link: AppLink) {
        openJob?.cancel()
        openJob = viewModelScope.launch {
            try {
                val event = when (link) {
                    is AppLink.Show -> AppLinkEvent.OpenShow(
                        showId = getShowDetailsUseCase.getShow(link.slug).ids.trakt,
                    )
                    is AppLink.Movie -> AppLinkEvent.OpenMovie(
                        movieId = getMovieDetailsUseCase.getMovie(link.slug).ids.trakt,
                    )
                }
                events.emit(event)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (error.getHttpCode() != HTTP_NOT_FOUND) {
                        Timber.recordError(error)
                    }
                }
                events.emit(AppLinkEvent.Error)
            }
        }
    }

    val events: Flow<AppLinkEvent>
        field = MutableSharedFlow<AppLinkEvent>(replay = 0)
}
