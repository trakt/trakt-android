package tv.trakt.trakt.core.streamings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.streamings.model.AllStreamingsSection
import tv.trakt.trakt.common.core.streamings.model.StreamingsRequest
import tv.trakt.trakt.common.core.streamings.usecase.GetAllStreamingsUseCase
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.YouTubeSpecial
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.streamings.navigation.AllStreamingsDestination
import java.util.Locale

@Suppress("UNCHECKED_CAST")
internal class AllStreamingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val getAllStreamingsUseCase: GetAllStreamingsUseCase,
    private val moviesRemoteSource: MoviesRemoteSource,
    private val analytics: Analytics,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<AllStreamingsDestination>()

    private val initialState = AllStreamingsState(
        media = AllStreamingsState.Media(
            title = route.mediaTitle,
            background = route.backgroundUrl,
        ),
    )

    private val sectionsState = MutableStateFlow(initialState.sections)
    private val youTubeSpecialState = MutableStateFlow(initialState.youTubeSpecial)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val user = sessionManager.getProfile()
                if (user == null || !sessionManager.isAuthenticated()) {
                    return@launch
                }

                loadYouTubeSpecial()

                val sections = getAllStreamingsUseCase.getStreamings(
                    user = user,
                    request = StreamingsRequest(
                        mediaType = route.mediaType,
                        mediaId = route.mediaId.toTraktId(),
                        seasonEpisode = route.season?.let { season ->
                            SeasonEpisode(
                                season = season,
                                episode = route.episode ?: 1,
                            )
                        },
                    ),
                )

                sectionsState.update { sections }
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

    private fun loadYouTubeSpecial() {
        if (route.mediaType != MediaType.Movie || !route.checkYouTubeSpecial) {
            return
        }

        viewModelScope.launch {
            try {
                val special = moviesRemoteSource
                    .getYouTubeSpecial(
                        movieId = route.mediaId.toTraktId(),
                        locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault(),
                    )
                    ?.let(YouTubeSpecial::fromDto)
                youTubeSpecialState.update { special }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun logStreamOn(service: StreamingService) {
        analytics.playback.logStreamOn(service.source)
    }

    fun logYouTubeSpecialStreamOn() {
        analytics.playback.logStreamOn(YouTubeSpecial.SOURCE)
    }

    val state = combine(
        sectionsState,
        loadingState,
        errorState,
        youTubeSpecialState,
    ) { state ->
        initialState.copy(
            sections = state[0] as ImmutableList<AllStreamingsSection>,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
            youTubeSpecial = state[3] as YouTubeSpecial?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
