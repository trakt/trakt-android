package tv.trakt.trakt.app.core.streamings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.streamings.navigation.AllStreamingsDestination
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.streamings.model.AllStreamingsSection
import tv.trakt.trakt.common.core.streamings.model.StreamingsRequest
import tv.trakt.trakt.common.core.streamings.usecase.GetAllStreamingsUseCase
import tv.trakt.trakt.common.core.streamings.usecase.GetYouTubeSpecialUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.streamings.YouTubeSpecial
import tv.trakt.trakt.common.model.toTraktId
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UNCHECKED_CAST")
internal class AllStreamingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val getAllStreamingsUseCase: GetAllStreamingsUseCase,
    private val getYouTubeSpecialUseCase: GetYouTubeSpecialUseCase,
) : ViewModel() {
    private val initialState = AllStreamingsState()

    private val sectionsState = MutableStateFlow(initialState.sections)
    private val youTubeSpecialState = MutableStateFlow(initialState.youTubeSpecial)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private val route = savedStateHandle.toRoute<AllStreamingsDestination>()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val user = sessionManager.getProfile()
                if (!sessionManager.isAuthenticated() || user == null) {
                    return@launch
                }

                loadingState.update { true }
                loadYouTubeSpecial()

                delay(500.milliseconds)
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

                Timber.d("Loaded streaming sections: %d", sections.size)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading streamings")
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    private fun loadYouTubeSpecial() {
        if (route.mediaType != MediaType.Movie || !route.checkYouTubeSpecial) {
            return
        }

        viewModelScope.launch {
            try {
                val special = getYouTubeSpecialUseCase.getYouTubeSpecial(route.mediaId.toTraktId())
                youTubeSpecialState.update { special }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error loading YouTube special")
                }
            }
        }
    }

    val state = combine(
        loadingState,
        sectionsState,
        errorState,
        youTubeSpecialState,
    ) { state ->
        AllStreamingsState(
            loading = state[0] as Boolean,
            sections = state[1] as ImmutableList<AllStreamingsSection>,
            error = state[2] as Exception?,
            youTubeSpecial = state[3] as YouTubeSpecial?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
