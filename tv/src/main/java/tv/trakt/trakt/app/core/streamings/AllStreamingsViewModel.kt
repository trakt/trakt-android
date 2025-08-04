package tv.trakt.trakt.app.core.streamings

import android.util.Log
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
import tv.trakt.trakt.app.common.model.SeasonEpisode
import tv.trakt.trakt.app.core.streamings.navigation.AllStreamingsDestination
import tv.trakt.trakt.app.core.streamings.usecase.GetAllStreamingsUseCase
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.toTraktId

internal class AllStreamingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val getAllStreamingsUseCase: GetAllStreamingsUseCase,
) : ViewModel() {
    private val initialState = AllStreamingsState()

    private val streamingsState = MutableStateFlow(initialState.services)
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

                val streamings = getAllStreamingsUseCase.getStreamings(
                    user = user,
                    mediaId = route.mediaId.toTraktId(),
                    mediaType = route.mediaType,
                    seasonEpisode = SeasonEpisode(
                        season = route.season ?: 0,
                        episode = route.episode ?: 1,
                    ),
                )
                streamingsState.update { streamings }

                Log.d("AllStreamingsViewModel", "Loaded streamings: ${streamings.size}")
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Log.e("AllStreamingsViewModel", "Error loading streamings", error)
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<AllStreamingsState> = combine(
        loadingState,
        streamingsState,
        errorState,
    ) { s1, s2, s3 ->
        AllStreamingsState(
            loading = s1,
            services = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
