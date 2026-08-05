package tv.trakt.trakt.core.streamings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.streamings.model.StreamingsRequest
import tv.trakt.trakt.common.core.streamings.usecase.GetAllStreamingsUseCase
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.streamings.navigation.AllStreamingsDestination

internal class AllStreamingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val getAllStreamingsUseCase: GetAllStreamingsUseCase,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<AllStreamingsDestination>()

    private val initialState = AllStreamingsState(
        media = AllStreamingsState.Media(
            title = route.mediaTitle,
            background = route.backgroundUrl,
        ),
    )

    private val sectionsState = MutableStateFlow(initialState.sections)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    val state: StateFlow<AllStreamingsState> = combine(
        sectionsState,
        loadingState,
        errorState,
    ) { sections, loading, error ->
        initialState.copy(
            sections = sections,
            loading = loading,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )

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
}
